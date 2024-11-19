package com.sky.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;

/**
 * 布隆过滤器工厂类
 */
public class BloomFilterFactory {

    // 私有化构造方法，防止外部实例化
    private BloomFilterFactory() {}

    /**
     * 创建布隆过滤器
     *
     * @param expectedInsertions 预期插入数量
     * @param fpp               错误率（误判率）
     * @return 布隆过滤器实例
     */
    public static BloomFilter<String> createBloomFilter(int expectedInsertions, double fpp) {
        return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, fpp);
    }
}