package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.common.domain.ID;
import com.penglecode.codeforce.common.domain.OrderBy;
import com.penglecode.codeforce.common.domain.Page;
import com.penglecode.codeforce.common.support.MapLambdaBuilder;
import com.penglecode.codeforce.common.util.DateTimeUtils;
import com.penglecode.codeforce.common.util.JsonUtils;
import com.penglecode.codeforce.mybatistiny.dsl.LambdaQueryCriteria;
import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.*;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.*;
import com.penglecode.codeforce.mybatistiny.support.EntityMapperHelper;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.RowBounds;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品模块的测试用例
 *
 * @author pengpeng
 * @version 1.0
 */
public abstract class ProductTestCase {

    /**
     * 创建商品(包括商品基本信息、额外信息、销售规格、销售库存)
     */
    protected void createProduct(ProductAggregate product) {
        getProductBaseInfoMapper().insert(product);
        Long productId = product.getProductId(); //数据库返回的productId(自增的)
        product.getProductExtra().setProductId(productId);
        getProductExtraInfoMapper().insert(product.getProductExtra());
        product.getProductSaleSpecs().forEach(item -> item.setProductId(productId));
        getProductSaleSpecMapper().batchUpdate(product.getProductSaleSpecs(), saleSpec -> getProductSaleSpecMapper().insert(saleSpec));
        product.getProductSaleStocks().forEach(item -> item.setProductId(productId));
        getProductSaleStockMapper().batchUpdate(product.getProductSaleStocks(), saleStock -> getProductSaleStockMapper().insert(saleStock));
    }

    /**
     * 根据ID更新
     */
    protected void updateProduct(Long productId) {
        ProductBaseInfo productBase = getProductBaseInfoMapper().selectById(productId);
        if(productBase != null) {
            String nowTime = DateTimeUtils.formatNow();

            productBase.setProductName(productBase.getProductName() + "AAA");
            productBase.setRemark("AAA");
            productBase.setAuditStatus(0);
            productBase.setOnlineStatus(0);
            Map<String,Object> updateColumns1 = MapLambdaBuilder.of(productBase)
                    .with(ProductBaseInfo::getProductName)
                    .with(ProductBaseInfo::getRemark)
                    .with(ProductBaseInfo::getAuditStatus)
                    .with(ProductBaseInfo::getOnlineStatus)
                    .withOverride(ProductBaseInfo::getUpdateTime, nowTime)
                    .build();
            getProductBaseInfoMapper().updateById(productBase.identity(), updateColumns1);

            QueryCriteria<ProductSaleStock> queryCriteria = LambdaQueryCriteria.ofSupplier(ProductSaleStock::new)
                    .eq(ProductSaleStock::getProductId, productBase.getProductId());
            List<ProductSaleStock> productSaleStocks = getProductSaleStockMapper().selectListByCriteria(queryCriteria);
            getProductSaleStockMapper().batchUpdate(productSaleStocks, saleStock -> {
                Map<String,Object> updateColumns2 = MapLambdaBuilder.of(saleStock)
                        .withOverride(ProductSaleStock::getSellPrice, saleStock.getSellPrice() - saleStock.getSellPrice() % 100)
                        .withOverride(ProductSaleStock::getUpdateTime, nowTime)
                        .build();
                getProductSaleStockMapper().updateById(saleStock.identity(), updateColumns2);
            });
        }
    }

    /**
     * 批量更新所有商品的售价
     */
    protected void batchUpdateAllProductSellPrice() {
        //查询所有库存记录
        List<ProductSaleStock> productSaleStocks = getProductSaleStockMapper().selectListByCriteria(LambdaQueryCriteria.ofSupplier(ProductSaleStock::new));
        //批量更新售价
        String nowTime = DateTimeUtils.formatNow();
        getProductSaleStockMapper().batchUpdate(productSaleStocks, saleStock -> {
            Map<String,Object> updateColumns2 = MapLambdaBuilder.of(saleStock)
                    .withOverride(ProductSaleStock::getSellPrice, saleStock.getSellPrice() - saleStock.getSellPrice() % 100)
                    .withOverride(ProductSaleStock::getUpdateTime, nowTime)
                    .build();
            getProductSaleStockMapper().updateById(saleStock.identity(), updateColumns2);
        });
    }

    /**
     * 根据ID查询
     */
    protected void selectById() {
        ProductBaseInfo productBase1 = getProductBaseInfoMapper().selectById(1L);
        System.out.println(JsonUtils.object2Json(productBase1));

        System.out.println("-----------------------------------------------");

        ProductBaseInfo productBase2 = getProductBaseInfoMapper().selectById(10L, new QueryColumns(ProductBaseInfo::getProductId, ProductBaseInfo::getProductName, ProductBaseInfo::getAuditStatus, ProductBaseInfo::getOnlineStatus));
        System.out.println(JsonUtils.object2Json(productBase2));

        System.out.println("-----------------------------------------------");

        ID id = new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "101");
        ProductSaleSpec productSaleSpec = getProductSaleSpecMapper().selectById(id);
        System.out.println(JsonUtils.object2Json(productSaleSpec));
    }

    /**
     * 根据多个ID查询
     */
    protected void selectByIds() {
        List<ProductBaseInfo> productBases = getProductBaseInfoMapper().selectListByIds(Arrays.asList(5L, 6L, 7L, 8L, 9L),
                new QueryColumns(ProductBaseInfo::getProductId, ProductBaseInfo::getProductName, ProductBaseInfo::getProductType));
        if(productBases != null) {
            productBases.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }

        List<ID> ids = new ArrayList<>();
        ids.add(new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "101"));
        ids.add(new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "102"));
        ids.add(new ID().addKey(ProductSaleSpec::getProductId, 1L).addKey(ProductSaleSpec::getSpecNo, "103"));
        List<ProductSaleSpec> productSaleSpecs = getProductSaleSpecMapper().selectListByIds(ids);
        if(productSaleSpecs != null) {
            productSaleSpecs.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 根据条件查询(单个)
     */
    protected void selectByCriteria() {
        QueryCriteria<ProductSaleSpec> queryCriteria = LambdaQueryCriteria.ofSupplier(ProductSaleSpec::new)
                .eq(ProductSaleSpec::getProductId, 1L)
                .eq(ProductSaleSpec::getSpecNo, "101");
        ProductSaleSpec productSaleSpec = getProductSaleSpecMapper().selectByCriteria(queryCriteria);
        System.out.println(JsonUtils.object2Json(productSaleSpec));
    }

    /**
     * 根据条件查询(多个)
     */
    protected void selectListByCriteria() {
        QueryCriteria<ProductBaseInfo> queryCriteria1 = LambdaQueryCriteria.ofSupplier(ProductBaseInfo::new)
                .eq(ProductBaseInfo::getProductType, 1)
                .orderBy(OrderBy.desc(ProductBaseInfo::getCreateTime))
                .limit(10);
        QueryColumns queryColumns = new QueryColumns(ProductBaseInfo::getProductId, ProductBaseInfo::getProductName, ProductBaseInfo::getProductType, ProductBaseInfo::getAuditStatus, ProductBaseInfo::getOnlineStatus);
        List<ProductBaseInfo> productBases = getProductBaseInfoMapper().selectListByCriteria(queryCriteria1, queryColumns);
        if(productBases != null) {
            productBases.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }

        System.out.println("-----------------------------------------------");

        ProductSaleStock queryRequest = new ProductSaleStock();
        queryRequest.setProductId(18L);
        queryRequest.setSpecNo("108");
        queryRequest.setMinStock(0);
        queryRequest.setMaxStock(150);
        QueryCriteria<ProductSaleStock> criteria2 = LambdaQueryCriteria.of(queryRequest)
                .eq(ProductSaleStock::getProductId)
                .likeRight(ProductSaleStock::getSpecNo)
                .between(ProductSaleStock::getStock, queryRequest.getMinStock(), queryRequest.getMaxStock())
                .orderBy(OrderBy.desc(ProductSaleStock::getSellPrice));
        List<ProductSaleStock> productStocks = getProductSaleStockMapper().selectListByCriteria(criteria2);
        if(productBases != null) {
            productStocks.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 根据动态条件查询
     */
    protected void selectByDynamicCriteria() {
        ProductBaseInfo queryRequest = new ProductBaseInfo();
        queryRequest.setProductName("HUAWEI");
        queryRequest.setProductType(1);
        //queryRequest.setOnlineStatus(1);
        queryRequest.setAuditStatuses(Arrays.asList(0,1,2));

        QueryCriteria<ProductBaseInfo> queryCriteria = LambdaQueryCriteria.of(queryRequest)
                .like(ProductBaseInfo::getProductName)
                .eq(ProductBaseInfo::getProductType)
                .eq(ProductBaseInfo::getOnlineStatus)
                .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                .orderBy(OrderBy.desc(ProductBaseInfo::getCreateTime))
                .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
        List<ProductBaseInfo> productBases = getProductBaseInfoMapper().selectListByCriteria(queryCriteria);
        if(productBases != null) {
            productBases.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 根据动态条件查询
     */
    protected void selectProductsByCondition() {
        ProductBaseInfo condition = new ProductBaseInfo();
        condition.setProductType(1);
        condition.setAuditStatuses(Arrays.asList(0,1,2));
        List<ProductBaseInfo> productBases = getProductBaseInfoMapper().selectProductsByCondition(condition);
        if(productBases != null) {
            productBases.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 根据动态条件查询(分页示例1)
     */
    protected void selectPageListByCriteria1() {
        Page page = Page.of(1, 10, OrderBy.desc(ProductBaseInfo::getCreateTime));
        ProductBaseInfo queryRequest = new ProductBaseInfo();
        queryRequest.setProductName("手机");
        queryRequest.setProductType(1);
        //queryRequest.setOnlineStatus(1);
        queryRequest.setAuditStatuses(Arrays.asList(0,1,2));

        QueryCriteria<ProductBaseInfo> queryCriteria = LambdaQueryCriteria.of(queryRequest)
                .and(nestedCriteria -> nestedCriteria.like(ProductBaseInfo::getProductName, "华为")
                        .or().like(ProductBaseInfo::getProductName, "HUAWEI"))
                .eq(ProductBaseInfo::getProductType)
                .eq(ProductBaseInfo::getOnlineStatus)
                .in(ProductBaseInfo::getAuditStatus, queryRequest.getAuditStatuses().toArray())
                .orderBy(page.getOrderBys())
                .dynamic(true); //自动过滤掉为空值(null|空串|空数组|空集合)的查询参数
        QueryColumns queryColumns = new QueryColumns(ProductBaseInfo::getProductId, ProductBaseInfo::getProductName, ProductBaseInfo::getProductType, ProductBaseInfo::getAuditStatus, ProductBaseInfo::getOnlineStatus);
        List<ProductBaseInfo> productBases = getProductBaseInfoMapper().selectPageListByCriteria(queryCriteria, new RowBounds(page.offset(), page.limit()), queryColumns);
        page.setTotalRowCount(getProductBaseInfoMapper().selectPageCountByCriteria(queryCriteria)); //设置总记录数
        System.out.println(page);
        if(productBases != null) {
            productBases.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 根据动态条件查询(分页示例2)
     */
    protected void selectPageListByCriteria2() {
        LocalDate nowDate = LocalDate.now();
        QueryCriteria<ProductBaseInfo> queryCriteria = LambdaQueryCriteria.ofSupplier(ProductBaseInfo::new)
                .eq(ProductBaseInfo::getProductType, 1)
                .between(ProductBaseInfo::getCreateTime, nowDate.with(TemporalAdjusters.firstDayOfMonth()), nowDate.with(TemporalAdjusters.lastDayOfMonth()))
                .orderBy(OrderBy.desc(ProductBaseInfo::getCreateTime))
                .limit(5) //limit遇到分页查询(selectPageListByCriteria)时会失效
                .dynamic(true);
        Page page = Page.of(2, 10);
        List<ProductBaseInfo> productBases = EntityMapperHelper.selectEntityObjectListByPage(getProductBaseInfoMapper(), queryCriteria, page);
        System.out.println(page);
        if(productBases != null) {
            productBases.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 通过游标查询所有记录(必须在一个事务中)
     */
    protected Object selectAllListByCursor() {
        Cursor<ProductBaseInfo> cursor = getProductBaseInfoMapper().selectAllList();
        cursor.forEach(productBase -> System.out.println(JsonUtils.object2Json(productBase)));
        return cursor.isConsumed();
    }

    /**
     * 测试在BaseEntityMapper子类中添加自定义方法
     */
    protected void selectAvgSellPrices() {
        List<ProductSaleStock> avgSellPrices = getProductSaleStockMapper().selectAvgSellPrices(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        if(avgSellPrices != null) {
            avgSellPrices.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 测试在非BaseEntityMapper子类的常规Mapper中的自定义方法
     */
    protected void selectPriceStatListByIds() {
        List<ProductPriceStat> productPriceStats = getProductStatisticsMapper().selectPriceStatListByIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        if(productPriceStats != null) {
            productPriceStats.forEach(item -> System.out.println(JsonUtils.object2Json(item)));
        }
    }

    /**
     * 根据Id删除记录
     */
    protected void deleteByCriteria() {
        getProductBaseInfoMapper().deleteById(2L);
        getProductExtraInfoMapper().deleteById(2L);

        QueryCriteria<ProductSaleSpec> queryCriteria1 = LambdaQueryCriteria.ofSupplier(ProductSaleSpec::new)
                .eq(ProductSaleSpec::getProductId, 2L);
        getProductSaleSpecMapper().deleteByCriteria(queryCriteria1);

        QueryCriteria<ProductSaleStock> queryCriteria2 = LambdaQueryCriteria.ofSupplier(ProductSaleStock::new)
                .eq(ProductSaleStock::getProductId, 2L);
        List<ProductSaleStock> productSaleStocks = getProductSaleStockMapper().selectListByCriteria(queryCriteria2);
        List<ID> ids = productSaleStocks.stream()
                .map(saleStock -> new ID().addKey(ProductSaleStock::getProductId, saleStock.getProductId()).
                        addKey(ProductSaleStock::getSpecNo, saleStock.getSpecNo()))
                .collect(Collectors.toList());
        getProductSaleStockMapper().deleteByIds(ids);
    }

    public abstract ProductBaseInfoMapper getProductBaseInfoMapper();

    public abstract ProductExtraInfoMapper getProductExtraInfoMapper();

    public abstract ProductSaleSpecMapper getProductSaleSpecMapper();

    public abstract ProductSaleStockMapper getProductSaleStockMapper();

    public abstract ProductStatisticsMapper getProductStatisticsMapper();

}
