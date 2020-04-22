package com.limit.common.utils;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseUtil {

    public static final String CHARSET_NAME = "UTF-8";

    /**
     * long 类型转化成 int 类型，超出 int 范围直接返回 MAX 或者 MIN
     * @param k 传入长整型变量
     * @return int 类型的值
     */
    public static int safeLongToInt(long k) {
        if (k > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        if (k < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return (int)k;
    }

    //日期格式
    public final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前时间。
     * @return 代表当前时间的字符串，格式为 "yyyy-MM-dd HH:mm:ss"。
     */
    public static String currentTime() {
        return df.format(new Date());
    }

    /**
     * 获取当前时间增加指定秒数之后的时间点。
     * @param seconds 时间偏移
     * @Return 代表时间的指定格式字符串，格式为 "yyyy-MM-dd HH:mm:ss"。
      */
    public static String timeAdd(int seconds) {
        Date now = new Date();
        return df.format(new Date(now.getTime() + seconds * 1000));
    }

    public static byte[] StringToByteArray(String message) throws Exception{
        return message.getBytes(CHARSET_NAME);
    }

    public static String ByteArrayToString(byte[] array) throws Exception {
        return new String(array);
    }

    public static byte[] ObjectToByteArray(Object message) throws Exception{
        String msg = beanToString(message);
        return msg.getBytes(CHARSET_NAME);
    }

    public static Object ByteArrayToObject(byte[] array, Class clazz) throws Exception {
        return stringToBean(new String(array), clazz);
    }

    public static Object stringToBean(String str, Class clazz) {
        if (str == null || str.length() == 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return Integer.valueOf(str);
        }else if(clazz == String.class) {
            return str;
        }else if(clazz == long.class || clazz == Long.class) {
            return  Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    public static String beanToString(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String)value;
        } else {
            return JSON.toJSONString(value);
        }
    }

}
