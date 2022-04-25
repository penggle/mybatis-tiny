package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.mybatistiny.examples.boot.MybatisTinyExampleApplication;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.*;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductAggregate;
import com.penglecode.codeforce.mybatistiny.examples.utils.ProductExampleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 商品模块的Mapper测试
 *
 * @author pengpeng
 * @version 1.0
 */
@SpringBootTest(classes=MybatisTinyExampleApplication.class)
public class ProductTestBySpringBoot extends ProductTestCase {

    @Autowired
    private ProductBaseInfoMapper productBaseInfoMapper;

    @Autowired
    private ProductExtraInfoMapper productExtraInfoMapper;

    @Autowired
    private ProductSaleSpecMapper productSaleSpecMapper;

    @Autowired
    private ProductSaleStockMapper productSaleStockMapper;

    @Autowired
    private ProductStatisticsMapper productStatisticsMapper;

    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

    protected <T> void doInTransaction(ExampleExecutor executor) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
        transactionTemplate.execute(status -> {
            executor.execute();
            return null;
        });
    }

    /**
     * 测试创建商品
     */
    @Test
    public void createProductTest() {
        List<ProductAggregate> exampleProductList = ProductExampleUtils.getExampleProductList();
        doInTransaction(() -> createProduct(exampleProductList.get(0)));
    }

    /**
     * 测试根据ID更新商品
     */
    @Test
    public void updateProductTest() {
        doInTransaction(() -> updateProduct(1L));
    }

    /**
     * 创建剩余商品
     */
    @Test
    public void createProductRemaining() {
        List<ProductAggregate> exampleProductList = ProductExampleUtils.getExampleProductList();
        doInTransaction(() -> {
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
        doInTransaction(this::batchUpdateAllProductSellPrice);
    }

    /**
     * 测试根据ID查询
     */
    @Test
    public void selectByIdTest() {
        selectById();
    }

    /**
     * 根据多个ID查询
     */
    @Test
    public void selectByIdsTest() {
        selectByIds();
    }

    /**
     * 测试根据条件查询(单个)
     */
    @Test
    public void selectByCriteriaTest() {
        selectByCriteria();
    }

    /**
     * 测试根据条件查询(多个)
     */
    @Test
    public void selectListByCriteriaTest() {
        selectListByCriteria();
    }

    /**
     * 测试根据动态条件查询
     */
    @Test
    public void selectByDynamicCriteriaTest() {
        selectByDynamicCriteria();
    }

    /**
     * 测试根据动态条件查询
     */
    @Test
    public void selectProductsByConditionTest() {
        selectProductsByCondition();
    }

    /**
     * 测试根据动态条件查询(分页示例1)
     */
    @Test
    public void selectPageListByCriteria1Test() {
        selectPageListByCriteria1();
    }

    /**
     * 根据动态条件查询(分页示例2)
     */
    @Test
    public void selectPageListByCriteria2Test() {
        selectPageListByCriteria2();
    }

    /**
     * 测试通过游标查询所有记录(必须在一个事务中)
     */
    @Test
    public void selectAllListByCursorTest() {
        doInTransaction(this::selectAllListByCursor);
    }

    /**
     * 测试在BaseEntityMapper子类中添加自定义方法
     */
    @Test
    public void selectAvgSellPricesTest() {
        selectAvgSellPrices();
    }

    /**
     * 测试在非BaseEntityMapper子类的常规Mapper中的自定义方法
     */
    @Test
    public void selectPriceStatListByIdsTest() {
        selectPriceStatListByIds();
    }

    /**
     * 测试根据Id删除记录
     */
    @Test
    public void deleteByCriteriaTest() {
        deleteByCriteria();
    }

    @Override
    public ProductBaseInfoMapper getProductBaseInfoMapper() {
        return productBaseInfoMapper;
    }

    @Override
    public ProductExtraInfoMapper getProductExtraInfoMapper() {
        return productExtraInfoMapper;
    }

    @Override
    public ProductSaleSpecMapper getProductSaleSpecMapper() {
        return productSaleSpecMapper;
    }

    @Override
    public ProductSaleStockMapper getProductSaleStockMapper() {
        return productSaleStockMapper;
    }

    @Override
    public ProductStatisticsMapper getProductStatisticsMapper() {
        return productStatisticsMapper;
    }

    public DataSourceTransactionManager getDataSourceTransactionManager() {
        return dataSourceTransactionManager;
    }

}
