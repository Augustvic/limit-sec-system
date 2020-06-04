package com.limit.common.cache;

import org.junit.Test;

public class CacheTest {

    @Test
    public void LFUCacheTest() {
        LFUCache<String, Integer> cache = new LFUCache<>("lfu", 3);
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        cache.put("one", 1);
        cache.put("two", 2);
        System.out.println(cache.get("one") == 1); // true
        System.out.println(cache.get("two") == 2); // true
        System.out.println(cache.get("three") == 3); // true
        System.out.println(cache.size() == 3); // true
        cache.put("four", 4);
        cache.put("four", 4);
        System.out.println(cache.size() == 3); // true
        System.out.println(cache.containsKey("one")); // true
        System.out.println(cache.containsKey("two")); // true
        System.out.println(cache.containsKey("four")); // true
        System.out.println(cache.containsKey("three")); // false
        cache.remove("four");
        System.out.println(cache.size() == 2); // true
        cache.put("five", 5);
        cache.put("five", 5);
        cache.put("five", 5);
        cache.put("four", 4);
        System.out.println(cache.containsKey("four")); // true
        System.out.println(cache.containsKey("five")); // true
        System.out.println(cache.containsKey("two")); // true
        System.out.println(cache.containsKey("one")); // false
        System.out.println(cache.size() == 3); // true
    }

    @Test
    public void LRUCacheTest() {
        LRUCache<String, Integer> cache = new LRUCache<>("lru", 3);
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        System.out.println(cache.get("one") == 1); // true
        System.out.println(cache.get("two") == 2); // true
        System.out.println(cache.get("three") == 3); // true
        System.out.println(cache.size() == 3); // true
        cache.put("four", 4);
        cache.put("four", 4);
        System.out.println(cache.size() == 3); // true
        System.out.println(cache.containsKey("one")); // false
        System.out.println(cache.containsKey("two")); // true
        System.out.println(cache.containsKey("three")); // true
        System.out.println(cache.containsKey("four")); // true
        cache.remove("four");
        System.out.println(cache.size() == 2); // true
        cache.put("five", 5);
        cache.put("five", 5);
        System.out.println(cache.containsKey("four")); // false
        System.out.println(cache.containsKey("five")); // true
        System.out.println(cache.containsKey("two")); // true
        System.out.println(cache.containsKey("three")); // true
        System.out.println(cache.size() == 3); // true
    }

    @Test
    public void LRU2CacheTest() {
        LRU2Cache<String, Integer> cache = new LRU2Cache<String, Integer>("lru2", 3);
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        cache.put("one", 1);
        cache.put("two", 2);
        cache.put("three", 3);
        System.out.println(cache.get("one") == 1); // true
        System.out.println(cache.get("two") == 2); // true
        System.out.println(cache.get("three") == 3); // true
        System.out.println(cache.size() == 3); // true
        cache.put("four", 4);
        cache.put("four", 4);
        System.out.println(cache.size() == 3); // true
        System.out.println(cache.containsKey("one")); // false
        System.out.println(cache.containsKey("two")); // true
        System.out.println(cache.containsKey("three")); // true
        System.out.println(cache.containsKey("four")); // true
        cache.remove("four");
        System.out.println(cache.size() == 2); // true
        cache.put("five", 5);
        cache.put("five", 5);
        System.out.println(cache.containsKey("four")); // false
        System.out.println(cache.containsKey("five")); // true
        System.out.println(cache.containsKey("two")); // true
        System.out.println(cache.containsKey("three")); // true
        System.out.println(cache.size() == 3); // true
        cache.put("six", 6);
        System.out.println(cache.containsKey("six")); // false
    }

}