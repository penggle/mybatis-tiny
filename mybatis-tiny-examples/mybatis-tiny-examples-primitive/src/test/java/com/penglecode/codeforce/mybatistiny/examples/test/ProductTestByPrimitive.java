package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.mybatistiny.core.DecoratedSqlSessionFactoryBuilder;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.*;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductAggregate;
import com.penglecode.codeforce.mybatistiny.examples.utils.ProductExampleUtils;
import com.penglecode.codeforce.mybatistiny.mapper.BaseMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品模块的Mapper测试
 *
 * @author pengpeng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class ProductTestByPrimitive extends ProductTestCase {

    private static final ThreadLocal<Map<Class<? extends BaseMapper>,? extends BaseMapper>> MAPPER_CONTEXT = new ThreadLocal<>();

    private static final Class<? extends BaseMapper>[] MAPPER_CLASSES = new Class[] {
            ProductBaseInfoMapper.class,
            ProductExtraInfoMapper.class,
            ProductSaleSpecMapper.class,
            ProductSaleStockMapper.class,
            ProductStatisticsMapper.class
    };

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    public void init() throws Exception {
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new DecoratedSqlSessionFactoryBuilder();
        this.sqlSessionFactory = sqlSessionFactoryBuilder.build(Resources.getResourceAsStream("mybatis-config.xml"));
    }

    protected <T> void executeWithMapper(ExampleExecutor executor) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            MAPPER_CONTEXT.set(Arrays.stream(MAPPER_CLASSES).collect(Collectors.toMap(Function.identity(), sqlSession::getMapper)));
            try {
                executor.execute();
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
            }
        } finally {
            MAPPER_CONTEXT.remove();
        }
    }

    /**
     * 测试创建商品
     */
    @Test
    public void createProductTest() {
        List<ProductAggregate> exampleProductList = ProductExampleUtils.getExampleProductList();
        executeWithMapper(() -> createProduct(exampleProductList.get(0)));
    }

    /**
     * 测试根据ID更新商品
     */
    @Test
    public void updateProductTest() {
        executeWithMapper(() -> updateProduct(1L));
    }

    /**
     * 创建剩余商品
     */
    @Test
    public void createProductRemaining() {
        List<ProductAggregate> exampleProductList = ProductExampleUtils.getExampleProductList();
        executeWithMapper(() -> {
            int size = exampleProductList.size();
            for(int i = 1; i < size; i++) {
                System.out.println("--------------------------------------------------");
                createProduct(exampleProductList.get(i));
            }
        });
    }

    /**
     * 测试批量更新所有商品的售价
     */
    @Test
    public void batchUpdateAllProductSellPriceTest() {
        executeWithMapper(this::batchUpdateAllProductSellPrice);
    }

    /**
     * 测试根据ID查询
     */
    @Test
    public void selectByIdTest() {
        executeWithMapper(this::selectById);
    }

    /**
     * 根据多个ID查询
     */
    @Test
    public void selectByIdsTest() {
        executeWithMapper(this::selectByIds);
    }

    /**
     * 测试根据条件查询(单个)
     */
    @Test
    public void selectByCriteriaTest() {
        executeWithMapper(this::selectByCriteria);
    }

    /**
     * 测试根据条件查询(多个)
     */
    @Test
    public void selectListByCriteriaTest() {
        executeWithMapper(this::selectListByCriteria);
    }

    /**
     * 测试根据动态条件查询
     */
    @Test
    public void selectByDynamicCriteriaTest() {
        executeWithMapper(this::selectByDynamicCriteria);
    }

    /**
     * 测试根据动态条件查询以及自动分页示例（这里自动分页不包括查COUNT）
     */
    @Test
    public void selectProductsByConditionTest() {
        executeWithMapper(this::selectProductsByCondition);
    }

    /**
     * 测试根据动态条件查询(分页示例1)
     */
    @Test
    public void selectPageListByCriteria1Test() {
        executeWithMapper(this::selectPageListByCriteria1);
    }

    /**
     * 根据动态条件查询(分页示例2)
     */
    @Test
    public void selectPageListByCriteria2Test() {
        executeWithMapper(this::selectPageListByCriteria2);
    }

    /**
     * 测试通过游标查询所有记录(必须在一个事务中)
     */
    @Test
    public void selectAllListByCursorTest() {
        executeWithMapper(this::selectAllListByCursor);
    }

    /**
     * 测试在BaseEntityMapper子类中添加自定义方法
     */
    @Test
    public void selectAvgSellPricesTest() {
        executeWithMapper(this::selectAvgSellPrices);
    }

    /**
     * 测试在非BaseEntityMapper子类的常规Mapper中的自定义方法
     */
    @Test
    public void selectPriceStatListByIdsTest() {
        executeWithMapper(this::selectPriceStatListByIds);
    }

    /**
     * 测试根据Id删除记录
     */
    @Test
    public void deleteByCriteriaTest() {
        executeWithMapper(this::deleteByCriteria);
    }

    @Override
    public ProductBaseInfoMapper getProductBaseInfoMapper() {
        return getThreadLocalMapper(ProductBaseInfoMapper.class);
    }

    @Override
    public ProductExtraInfoMapper getProductExtraInfoMapper() {
        return getThreadLocalMapper(ProductExtraInfoMapper.class);
    }

    @Override
    public ProductSaleSpecMapper getProductSaleSpecMapper() {
        return getThreadLocalMapper(ProductSaleSpecMapper.class);
    }

    @Override
    public ProductSaleStockMapper getProductSaleStockMapper() {
        return getThreadLocalMapper(ProductSaleStockMapper.class);
    }

    @Override
    public ProductStatisticsMapper getProductStatisticsMapper() {
        return getThreadLocalMapper(ProductStatisticsMapper.class);
    }

    protected <T extends BaseMapper> T getThreadLocalMapper(Class<T> mapperClass) {
        return (T) MAPPER_CONTEXT.get().get(mapperClass);
    }

}
