package com.miaosha.util;

public class BaseUtil {

    public static int safeIntToLong(long k) {
        if (k > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        if (k < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        return (int)k;
    }
}
