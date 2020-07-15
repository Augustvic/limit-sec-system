package com.limit.common.cache;

import com.limit.common.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final int maxCapacity;
    private final Map<K, HitValue> cache;
    private final Map<Integer, LinkedList<HitValue>> frequency;
    private int minFrequency;

    public LFUCache(String name) {
        this(name, Constants.CACHE_MAX_CAPACITY);
    }

    public LFUCache(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.cache = new HashMap<>();
        this.frequency = new HashMap<>();
        this.minFrequency = 1;
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                // increment hit count of key, and move the HitValue to next bucket
                HitValue hitValue= addHitCount(key);
                // update the value of key
                hitValue.value = value;
                // update minFrequency
                updateMinFrequency(hitValue, Constants.LFU_UPDATE);
            } else {
                // check and eliminate
                removeEldest();
                // put new HitValue into cache
                HitValue hitValue = new HitValue(key, value, 1);
                cache.put(key, hitValue);
                if (!frequency.containsKey(1)) {
                    frequency.put(1, new LinkedList<>());
                }
                frequency.get(1).addLast(hitValue);
                // update minFrequency
                updateMinFrequency(hitValue, Constants.LFU_PUT);
            }

        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                // increment hit count of key, and move the HitValue to next bucket
                HitValue hitValue = addHitCount(key);
                // update minFrequency
                updateMinFrequency(hitValue, Constants.LFU_GET);
                return hitValue.value;
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public V remove(K key) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                // remove HitValue in cache or frequency
                HitValue hitValue = cache.remove(key);
                frequency.get(hitValue.hitCount).remove(hitValue);
                if (frequency.get(hitValue.hitCount).isEmpty()) {
                    frequency.remove(hitValue.hitCount);
                }
                // update minFrequency
                updateMinFrequency(null, Constants.LFU_REMOVE);
                return hitValue.value;
            } else {
                return null;
            }
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

    // addHitCount
    private HitValue addHitCount(K key) {
        HitValue hitValue = cache.get(key);
        int hitCount = hitValue.hitCount;
        hitValue.incr();
        LinkedList<HitValue> oldList = frequency.get(hitCount);
        oldList.remove(hitValue);
        if (frequency.get(hitCount).isEmpty()) {
            frequency.remove(hitCount);
        }
        if (!frequency.containsKey(hitCount + 1)) {
            frequency.put(hitCount + 1, new LinkedList<>());
        }
        LinkedList<HitValue> newList = frequency.get(hitCount + 1);
        newList.addLast(hitValue);
        return hitValue;
    }

    // eliminate
    private void removeEldest() {
        if (cache.size() >= maxCapacity) {
            LinkedList<HitValue> list = frequency.get(minFrequency);
            HitValue removedHitValue = list.removeFirst();
            if (list.isEmpty()) {
                frequency.remove(minFrequency);
            }
            cache.remove(removedHitValue.key);
        }
    }

    // update minFrequency
    // The “remove” mode should traverse the whole map to find the least
    // frequency hit count, it will take a lot of time. So try not to use "remove" method.
    private void updateMinFrequency(HitValue hitValue, String mode) {
        if (mode.equals(Constants.LFU_PUT)) {
            // put
            minFrequency = 1;
        } else if (mode.equals(Constants.LFU_REMOVE)) {
            // remove
            minFrequency = Collections.min(frequency.keySet());
        } else {
            // get,update
            if ((minFrequency == hitValue.hitCount - 1) &&
                    (!frequency.containsKey(hitValue.hitCount - 1)
                            || frequency.get(hitValue.hitCount - 1).isEmpty())) {
                minFrequency++;
            }
        }
    }

    public String getName() {
        return name;
    }

    // inner class which record hit value of cache
    private class HitValue{
        final K key;
        V value;
        int hitCount;

        HitValue(K key, V value, int hitCount) {
            this.key = key;
            this.value = value;
            this.hitCount = hitCount;
        }

        void incr() {
            this.hitCount++;
        }
    }
}
