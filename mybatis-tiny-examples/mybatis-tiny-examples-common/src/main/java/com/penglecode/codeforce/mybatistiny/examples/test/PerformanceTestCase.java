package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.common.model.OrderBy;
import com.penglecode.codeforce.mybatistiny.dsl.LambdaQueryCriteria;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.ProductBaseInfoMapper;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductBaseInfo;
import org.apache.ibatis.session.RowBounds;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 性能测试用例(基于JMH的)
 *
 * @BenchmarkMode注解示例
 *
 * Mode.Throughput      计算一个时间单位内操作数量
 * Mode.AverageTime	    计算平均运行时间
 * Mode.SampleTime	    计算一个方法的运行时间(包括百分位)
 * Mode.SingleShotTime	方法仅运行一次(用于冷测试模式)。或者特定批量大小的迭代多次运行(具体查看后面的“@Measurement“注解)——这种情况下JMH将计算批处理运行时间(一次批处理所有调用的总时间)
 * 这些模式的任意组合	    可以指定这些模式的任意组合——该测试运行多次(取决于请求模式的数量)
 * Mode.All	            所有模式依次运行
 *
 * Scope.Thread     默认状态。实例将不共享分配给运行给定测试的每个线程。
 * Scope.Benchmark	运行相同测试的所有线程将共享实例。可以用来测试状态对象的多线程性能(或者仅标记该范围的基准)。这个配合@Threads(nThreads)注解来设置并发线程数
 * Scope.Group	    实例分配给每个线程组(查看后面的线程组部分)。这个配合@Group("your group name")注解一起使用
 *
 * @author pengpeng
 * @version 1.0
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1) //代码预热5轮，每轮循环执行该方法持续1秒
@Measurement(iterations = 5, time = 1) //每个待测方法执行5轮，每轮循环执行该方法持续1秒
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1) //启动的JVM进程数量
@Threads(16) //多线程并发测试的测试线程数
public abstract class PerformanceTestCase {

    /**
     * 根据动态条件查询
     */
    protected List<ProductBaseInfo> selectProductsByCriteria() {
        ProductBaseInfo queryRequest = new ProductBaseInfo();
        queryRequest.setProductType(1);
        queryRequest.setAuditStatuses(Arrays.asList(0,1,2));

        QueryCriteria<ProductBaseInfo> queryCriteria = LambdaQueryCriteria.of(queryRequest)
                .and(nestedCriteria -> nestedCriteria.like(ProductBaseInfo::getProductName, "华为")
                        .or().like(ProductBaseInfo::getProductName, "HUAWEI"))
                .eq(ProductBaseInfo::getProductType)
                .eq(ProductBaseInfo::getOnlineStatus)
                .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                .orderBy(OrderBy.desc(ProductBaseInfo::getCreateTime))
                .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
        return getProductBaseInfoMapper().selectPageListByCriteria(queryCriteria, new RowBounds(5,5));
    }

    /**
     * 根据动态条件查询
     */
    protected List<ProductBaseInfo> selectProductsByCondition() {
        ProductBaseInfo condition = new ProductBaseInfo();
        condition.setProductType(1);
        condition.setAuditStatuses(Arrays.asList(0,1,2));
        return getProductBaseInfoMapper().selectProductsByCondition(condition, new RowBounds(5,5));
    }

    public abstract ProductBaseInfoMapper getProductBaseInfoMapper();

}
