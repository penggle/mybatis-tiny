package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.mybatistiny.examples.config.MybatisConfiguration;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.ProductBaseInfoMapper;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductBaseInfo;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * 基于JMH的性能测试
 *
 * @author pengpeng
 * @version 1.0
 */
public class PerformanceTestBySpring extends PerformanceTestCase {

    private ConfigurableApplicationContext applicationContext;

    private ProductBaseInfoMapper productBaseInfoMapper;

    /**
     * 测试前避免刷日志，日志级别改成ERROR
     */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder().include(PerformanceTestBySpring.class.getSimpleName())
                .build();
        new Runner(options).run();
    }

    @Setup
    public void init() {
        this.applicationContext = new AnnotationConfigApplicationContext(MybatisConfiguration.class);
        this.applicationContext.start();
        this.productBaseInfoMapper = this.applicationContext.getBean(ProductBaseInfoMapper.class);
    }

    @TearDown
    public void close() {
        this.applicationContext.close();
    }

    @Benchmark
    public List<ProductBaseInfo> selectProductsByCriteriaTest() {
        return selectProductsByCriteria();
    }

    @Benchmark
    public List<ProductBaseInfo> selectProductsByConditionTest() {
        return selectProductsByCondition();
    }

    @Override
    public ProductBaseInfoMapper getProductBaseInfoMapper() {
        return productBaseInfoMapper;
    }

    protected ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
