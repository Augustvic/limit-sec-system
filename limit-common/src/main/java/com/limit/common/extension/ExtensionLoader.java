package com.limit.common.extension;

import com.limit.common.utils.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    // 扩展的接口
    private final Class<?> type;

    private final ExtensionFactory objectFactory;

    // 缓存的默认扩展名，就是 @SPI 中设置的值
    private String cachedDefaultName;

    // 扩展 Wrapper 实现类集合
    private Set<Class<?>> cachedWrapperClasses;

    // 扩展类与扩展名映射
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

    // 扩展名与扩展类的映射，和 cachedNames 正好相反
    // Holder 类位于 common 模块中，仅有一个 value 属性，此处相当于在 Map 外面加了一层而已。此处的 Holder 的 value 即为 Map。
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();

    // 扩展名与扩展对象的映射，例如 “dubbo” 与 DubboProcotol
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();

    // ExtensionLoader 集合， key 表示接口，value 表示对应的 ExtensionLoader
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    // 对象实例集合，key 表示实现类，value 表示对应的对象
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

    // 扩展名与加载对应拓展类发生的异常的映射
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final String DUBBO_DIRECTORY = "META-INF/limit/";

    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";

    private ExtensionLoader(Class<?> type) {
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null : new SpiExtensionFactory());
    }

    // 静态方法，通过类名调用
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        // 扩展点接口为 null
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        // 扩展点接口不是个接口
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        // 检查是否为扩展点接口
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }

        // 看缓存中有没有
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        // 缓存中没有，创建该接口的加载器，然后添加到缓存中
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    /**
     * 根据给定参数（扩展名）找到指定的扩展点实现，如果没找到，抛出 IllegalStateException 异常
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        // 默认实现
        if (name == null || name.length() == 0)
            return getDefaultExtension();
        // 从缓存中查找
        Holder<Object> holder = cachedInstances.get(name);
        // 缓存中没找到，创建一个 name-Holder 键值对放入 Map，此时 Holder 中没有元素
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }

        Object instance = holder.get();
        if (instance == null) {
            // Holder 已经在 Map 缓存中了，instance 为 null 说明里面没有要找的对象，现在要做的是创建一个新的扩展对象放入缓存中，然后返回
            synchronized (holder) {
                instance = holder.get();
                // 双重检查
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    public T getDefaultExtension() {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        return getExtension(cachedDefaultName);
    }

    // 创造扩展对象
    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        // 加载所有的扩展类，把缓存也初始化了
        Class<?> clazz = getExtensionClasses().get(name);
        // 扩展类不存在，抛出异常
        if (clazz == null) {
            throw findException(name);
        }
        try {
            // 尝试从缓存获取
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                // 缓存中没有，通过反射构造实例
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            // Dubbo 的 IOC 实现
            // 注入依赖（通过 setter）
            injectExtension(instance);
            // 判断是否在 Wrapper 的缓存中
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
                // 循环创建 Wrapper 实例
                for (Class<?> wrapperClass : wrapperClasses) {
                    // Dubbo 的 AOP 实现
                    // 将当前 instance 作为参数传给 Wrapper 的构造方法，并通过反射创建 Wrapper 实例，
                    // 然后向 Wrapper 实例中注入依赖，最后将 Wrapper 实例再次赋值给 instance 变量
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private Map<String, Class<?>> getExtensionClasses() {
        // 获取缓存中扩展名-扩展类的映射
        Map<String, Class<?>> classes = cachedClasses.get();
        // 缓存中的 Map 不存在，说明是第一次，缓存都还没有初始化
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                // 双重检查
                if (classes == null) {
                    // 获得所有的扩展类，并存入到 cachedClassed 中
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    // synchronized in getExtensionClasses
    private Map<String, Class<?>> loadExtensionClasses() {
        // 获取接口上的 @SPI 注释
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            // @SPI 注释里的值
            String value = defaultAnnotation.value();
            // trim 去掉字符串首尾的空格
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                // 只允许有一个默认值
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) cachedDefaultName = names[0];
            }
        }
        // 上面的操作是把 @SPI 注释里的扩展名赋值给 cachedDefaultName

        // 接下来的操作是加载指定文件夹的配置文件
        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadDirectory(extensionClasses, DUBBO_DIRECTORY);
        loadDirectory(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    // 从配置文件 dir 中，加载扩展接口所有的实现类
    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
        // 完整文件路径名
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            // 类加载器
            ClassLoader classLoader = findClassLoader();
            // 获取 urls，这里和 jdk 中 ServiceLoader 一样
            // 根据文件名加载所有同名文件
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    // 调用 loadResource
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

    // 逐行加载所有的类
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), "utf-8"));
            try {
                String line;
                // 按行读取
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    // 去掉注释，只需要 '#' 之前的部分
                    if (ci >= 0) line = line.substring(0, ci);
                    // 去掉前后空格
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            // 定位 ‘=’
                            int i = line.indexOf('=');
                            if (i > 0) {
                                // ‘=’ 之前的为扩展名，之后的为具体的扩展接口实现类
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0) {
                                // 调用 Class.forName 通过反射加载类，然后在 loadClass 方法中操作前面提到的一系列缓存
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                            exceptions.put(line, e);
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    // 操作以 cached 开头的缓存
    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + "is not subtype of interface.");
        }
        // 如果没有构造函数抛出异常
        clazz.getConstructor();
        // 切分 name
        String[] names = NAME_SEPARATOR.split(name);
        if (names != null && names.length > 0) {
            for (String n : names) {
                // 存储到扩展类-扩展名映射
                if (!cachedNames.containsKey(clazz)) {
                    cachedNames.put(clazz, n);
                }
                // 把加载到的类保存到 extensionClasses 里，然后返回
                Class<?> c = extensionClasses.get(n);
                if (c == null) {
                    extensionClasses.put(n, clazz);
                } else if (c != clazz) {
                    throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                }
            }
        }
    }

    private IllegalStateException findException(String name) {
        return new IllegalStateException(name);
    }

    // 注入依赖
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                // 获取该类中所有的方法
                for (Method method : instance.getClass().getMethods()) {
                    // 如果是 set 方法，只有一种参数类型，是 public 方法
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        // 参数类型
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            // 获取属性的名字，比如 setName 的属性名为 name
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            // 从 ObjectFactory 获取依赖对象
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                // 通过反射调用 setter 方法，注入依赖
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }
}
