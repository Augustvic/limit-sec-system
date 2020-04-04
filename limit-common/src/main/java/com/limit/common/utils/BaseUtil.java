package com.limit.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseUtil {

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

}
