package com.limit.common.cache;

import com.limit.common.Constants;

import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LRU-2
 * </p>
 * When the data accessed for the first time, add it to history list. If the size of history list reaches max capacity, eliminate the earliest data (first in first out).
 * When the data already exists in the history list, and be accessed for the second time, then it will be put into cache.
 */
public class LRU2Cache<K, V> extends LinkedHashMap<K, V> {

    private final String name;
    private final Lock lock = new ReentrantLock();
    private volatile int maxCapacity;

    // as history list
    private LRUCache<K, Boolean> preCache;

    public LRU2Cache(String name) {
        this(name, Constants.CACHE_MAX_CAPACITY);
    }

    public LRU2Cache(String name, int maxCapacity) {
        super(16, Constants.DEFAULT_LOAD_FACTOR, true);
        this.maxCapacity = maxCapacity;
        this.name = name;
        this.preCache = new LRUCache<>(name, maxCapacity);
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

    @Override
    public boolean containsKey(Object key) {
        lock.lock();
        try {
            return super.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(Object key) {
        lock.lock();
        try {
            return super.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
            if (preCache.containsKey(key)) {
                // add it to cache
                preCache.remove(key);
                return super.put(key, value);
            } else {
                // add it to history list
                preCache.put(key, true);
                return value;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.lock();
        try {
            preCache.remove(key);
            return super.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return super.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            preCache.clear();
            super.clear();
        } finally {
            lock.unlock();
        }
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        preCache.setMaxCapacity(maxCapacity);
        this.maxCapacity = maxCapacity;
    }

    public String getName() {
        return name;
    }
}
