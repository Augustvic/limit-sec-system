package com.limit.common.cache;

import org.junit.Test;

import static org.junit.Assert.*;

public class CacheFactoryTest {

    @Test
    public void getCache() {
        CacheFactory cacheFactory = new CacheFactory();
        Object cache = cacheFactory.getCache("lru2");
        System.out.println(cache instanceof LRU2Cache); // true
        LRU2Cache lru2Cache = (LRU2Cache)cache;
        System.out.println(lru2Cache.getMaxCapacity()); // 10000
        System.out.println(lru2Cache.getName()); // lru2
    }

    @Test
    public void getFIFOCache() {
        CacheFactory cacheFactory = new CacheFactory();
        Object cache = cacheFactory.getCache("fifo", "FIFO", 100);
        System.out.println(cache == null); // true
    }
}