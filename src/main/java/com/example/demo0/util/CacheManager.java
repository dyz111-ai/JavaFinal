package com.example.demo0.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 简单的内存缓存管理器
 * 使用ConcurrentHashMap实现线程安全的缓存
 */
public class CacheManager {
    
    /**
     * 缓存项，包含值和过期时间
     */
    private static class CacheItem<V> {
        private final V value;
        private final long expireTime;
        
        public CacheItem(V value, long ttl, TimeUnit timeUnit) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + timeUnit.toMillis(ttl);
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
        
        public V getValue() {
            return value;
        }
    }
    
    // 缓存容器，使用ConcurrentHashMap保证线程安全
    private final Map<String, CacheItem<?>> cache = new ConcurrentHashMap<>();
    
    private static volatile CacheManager instance;
    
    // 单例模式
    private CacheManager() {}
    
    /**
     * 获取缓存管理器实例
     */
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 存储缓存项
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 生存时间
     * @param timeUnit 时间单位
     */
    public <V> void put(String key, V value, long ttl, TimeUnit timeUnit) {
        if (key == null || value == null) {
            return;
        }
        cache.put(key, new CacheItem<>(value, ttl, timeUnit));
    }
    
    /**
     * 获取缓存项
     * @param key 缓存键
     * @return 缓存值，如果缓存不存在或已过期则返回null
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        if (key == null) {
            return null;
        }
        CacheItem<?> item = cache.get(key);
        if (item == null) {
            return null;
        }
        if (item.isExpired()) {
            cache.remove(key);
            return null;
        }
        return (V) item.getValue();
    }
    
    /**
     * 删除缓存项
     * @param key 缓存键
     */
    public void remove(String key) {
        cache.remove(key);
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * 获取缓存大小
     */
    public int size() {
        // 清理过期项并返回实际大小
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return cache.size();
    }
}