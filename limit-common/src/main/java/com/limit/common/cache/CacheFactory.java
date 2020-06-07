package com.limit.common.cache;

import com.limit.common.Constants;
import com.limit.common.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheFactory implements Factory {

//    private static final Map<String, LRUCache> LRU_CACHE_MAP = new ConcurrentHashMap<>();
//    private static final Map<String, LRU2Cache> LRU2_CACHE_MAP = new ConcurrentHashMap<>();
//    private static final Map<String, LFUCache> LFU_CACHE_MAP = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(CacheFactory.class);
    private static final Map<String, Object> CACHES= new ConcurrentHashMap<>();
    private static final Map<Object, String> CACHE_NAME= new ConcurrentHashMap<>();
    private static final Set<String> TYPES = new HashSet<>();
    static {
        TYPES.add(Constants.LRU_CACHE);
        TYPES.add(Constants.LFU_CACHE);
        TYPES.add(Constants.LRU2_CACHE);
    }

//    public LRUCache getLRUCache(String name, int maxCapacity) {
//        LRUCache cache = LRU_CACHE_MAP.get(name);
//        if (cache == null) {
//            LRU_CACHE_MAP.putIfAbsent(name, new LRUCache(name, maxCapacity));
//            cache = LRU_CACHE_MAP.get(name);
//        }
//        return cache;
//    }
//
//    public LRU2Cache getLRU2Cache(String name, int maxCapacity) {
//        LRU2Cache cache = LRU2_CACHE_MAP.get(name);
//        if (cache == null) {
//            LRU2_CACHE_MAP.putIfAbsent(name, new LRU2Cache(name, maxCapacity));
//            cache = LRU2_CACHE_MAP.get(name);
//        }
//        return cache;
//    }
//
//    public LFUCache getLFUCache(String name, int maxCapacity) {
//        LFUCache cache = LFU_CACHE_MAP.get(name);
//        if (cache == null) {
//            LFU_CACHE_MAP.putIfAbsent(name, new LFUCache(name, maxCapacity));
//            cache = LFU_CACHE_MAP.get(name);
//        }
//        return cache;
//    }

    public Object getCache(String name) {
        return getCache(name, Constants.LRU2_CACHE, Constants.CACHE_MAX_CAPACITY);
    }

    public Object getCache(String name, int maxCapacity) {
        return getCache(name, Constants.LRU2_CACHE, maxCapacity);
    }

    public Object getCache(String name, String type, int maxCapcity) {
        Object cache =  CACHES.get(name);
        if (cache == null) {
            if (!TYPES.contains(type)) {
                log.info("The type of " + type + "[" + name + "]" + " cache is not supported!");
                return null;
            } else {
                String realType = "com.limit.common.cache." + type + "Cache";
                try {
                    Class c = Class.forName(realType);
                    Constructor constructor = c.getConstructor(String.class, int.class);
                    cache = constructor.newInstance(name, maxCapcity);
                } catch (Exception e) {
                    log.info(e.toString());
                }
                CACHES.putIfAbsent(name, cache);
                CACHE_NAME.putIfAbsent(cache, name);
                cache = CACHES.get(name);
            }
        }
        return cache;
    }

    @Override
    public void destroy(Object obj) {
        String name = CACHE_NAME.remove(obj);
        CACHES.remove(name);
    }
}
