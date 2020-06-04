package com.limit.common.cache;

import com.limit.common.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LFU
 * </p>
 * Compare the hit count of cache and eliminate the least one.
 * If the least hit count cache has more than one, then compare latest access time.
 * Whatever happened, the put operation will put the new one into cache
 */
public class LFUCache<K, V> {

    private final String name;
    private final Lock lock = new ReentrantLock();
    private volatile int maxCapacity;
    private final Map<K, V> cache;
    private final Map<K, HitValue> count;

    public LFUCache(String name) {
        this(name, Constants.CACHE_MAX_CAPACITY);
    }

    public LFUCache(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.cache = new HashMap<>();
        this.count = new HashMap<>();
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            V oldValue = cache.get(key);
            HitValue hitValue;
            if (oldValue == null) {
                while (maxCapacity <= cache.size()) {
                    removeEldest();
                }
                cache.put(key, value);
                hitValue = new HitValue(key, 1, System.nanoTime());
                count.put(key, hitValue);
            } else {
                hitValue = count.get(key);
                hitValue.incr();
                hitValue.now();
            }
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        lock.lock();
        try {
            V oldValue = cache.get(key);
            if (oldValue != null) {
                HitValue hitValue = count.get(key);
                hitValue.incr();
                hitValue.now();
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    public void remove(K key) {
        lock.lock();
        try {
            cache.remove(key);
            count.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return cache.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean containsKey(K key) {
        lock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    private void removeEldest() {
        HitValue hitValue = Collections.min(count.values());
        cache.remove(hitValue.key);
        count.remove(hitValue.key);
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getName() {
        return name;
    }

    // inner class which record hit value and latest access time of cache
    class HitValue implements Comparable<HitValue>{
        final K key;
        int hitCount;
        long lastTime;

        HitValue(K key, int hitCount, long lastTime) {
            this.key = key;
            this.hitCount = hitCount;
            this.lastTime = lastTime;
        }

        void incr() {
            this.hitCount++;
        }

        void now() {
            this.lastTime = System.nanoTime();
        }

        @Override
        public int compareTo(HitValue o) {
            int compare = Integer.compare(this.hitCount, o.hitCount);
            return compare == 0 ? Long.compare(this.lastTime, o.lastTime) : compare;
        }
    }
}
