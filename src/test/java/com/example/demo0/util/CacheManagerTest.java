package com.example.demo0.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CacheManager 单元测试
 * 测试缓存的核心功能：基本操作、过期机制、线程安全等
 */
class CacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 使用单例模式获取缓存实例
        cacheManager = CacheManager.getInstance();
        // 在每个测试前清空缓存，确保测试独立性
        cacheManager.clear();
    }

    /**
     * 测试基本的put/get操作
     */
    @Test
    void testBasicPutAndGet() {
        // 存储缓存
        String key = "test:key";
        String value = "test value";
        cacheManager.put(key, value, 5, TimeUnit.MINUTES);

        // 获取缓存
        String retrievedValue = cacheManager.get(key);

        // 验证结果
        assertNotNull(retrievedValue);
        assertEquals(value, retrievedValue);
    }

    /**
     * 测试缓存不存在的情况
     */
    @Test
    void testGetNonExistentKey() {
        // 尝试获取不存在的缓存
        String retrievedValue = cacheManager.get("non:existent:key");

        // 验证结果
        assertNull(retrievedValue);
    }

    /**
     * 测试缓存过期
     */
    @Test
    void testCacheExpiration() throws InterruptedException {
        // 存储短期缓存(2秒过期)
        String key = "temp:key";
        String value = "temp value";
        cacheManager.put(key, value, 2, TimeUnit.SECONDS);

        // 立即获取(未过期)
        String retrievedValue1 = cacheManager.get(key);
        assertNotNull(retrievedValue1);
        assertEquals(value, retrievedValue1);

        // 等待3秒(已过期)
        Thread.sleep(3000);

        // 再次获取(已过期)
        String retrievedValue2 = cacheManager.get(key);
        assertNull(retrievedValue2);
    }

    /**
     * 测试键唯一性
     */
    @Test
    void testKeyUniqueness() {
        // 存储第一个值
        String key = "unique:key";
        String value1 = "value1";
        cacheManager.put(key, value1, 5, TimeUnit.MINUTES);

        // 存储相同键的第二个值
        String value2 = "value2";
        cacheManager.put(key, value2, 5, TimeUnit.MINUTES);

        // 获取缓存
        String retrievedValue = cacheManager.get(key);

        // 验证结果是最新的值
        assertNotNull(retrievedValue);
        assertEquals(value2, retrievedValue);
    }

    /**
     * 测试null值处理
     */
    @Test
    void testNullValues() {
        // 尝试存储null键和null值
        cacheManager.put(null, "value", 5, TimeUnit.MINUTES);
        cacheManager.put("key", null, 5, TimeUnit.MINUTES);

        // 验证结果
        assertNull(cacheManager.get(null));
        assertNull(cacheManager.get("key"));
    }

    /**
     * 测试线程安全
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        final AtomicInteger successCount = new AtomicInteger(0);

        // 创建多个线程同时操作缓存
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread:" + threadId + ":op:" + j;
                        String value = "value:" + threadId + ":" + j;
                        
                        // 存储缓存
                        cacheManager.put(key, value, 1, TimeUnit.MINUTES);
                        
                        // 获取缓存并验证
                        String retrievedValue = cacheManager.get(key);
                        if (value.equals(retrievedValue)) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证所有操作都成功
        int totalOperations = threadCount * operationsPerThread;
        assertEquals(totalOperations, successCount.get(), "线程安全测试失败，存在数据不一致的情况");
    }

    /**
     * 测试缓存大小
     */
    @Test
    void testCacheSize() throws InterruptedException {
        // 存储3个缓存项
        cacheManager.put("key1", "value1", 5, TimeUnit.MINUTES);
        cacheManager.put("key2", "value2", 5, TimeUnit.MINUTES);
        cacheManager.put("key3", "value3", 1, TimeUnit.SECONDS);

        // 立即检查大小
        assertEquals(3, cacheManager.size());

        // 等待过期
        Thread.sleep(2000);

        // 再次检查大小(过期项被清理)
        assertEquals(2, cacheManager.size());
    }

    /**
     * 测试删除缓存
     */
    @Test
    void testRemoveCache() {
        // 存储缓存
        String key = "remove:test:key";
        String value = "remove test value";
        cacheManager.put(key, value, 5, TimeUnit.MINUTES);

        // 验证缓存存在
        assertNotNull(cacheManager.get(key));

        // 删除缓存
        cacheManager.remove(key);

        // 验证缓存已删除
        assertNull(cacheManager.get(key));
    }

    /**
     * 测试清空缓存
     */
    @Test
    void testClearCache() {
        // 存储多个缓存项
        cacheManager.put("key1", "value1", 5, TimeUnit.MINUTES);
        cacheManager.put("key2", "value2", 5, TimeUnit.MINUTES);
        cacheManager.put("key3", "value3", 5, TimeUnit.MINUTES);

        // 验证缓存数量
        assertEquals(3, cacheManager.size());

        // 清空缓存
        cacheManager.clear();

        // 验证缓存已清空
        assertEquals(0, cacheManager.size());
        assertNull(cacheManager.get("key1"));
        assertNull(cacheManager.get("key2"));
        assertNull(cacheManager.get("key3"));
    }
}
