package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductSaleStock;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品销售库存Mapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface ProductSaleStockMapper extends BaseEntityMapper<ProductSaleStock> {

    List<ProductSaleStock> selectAvgSellPrices(List<Long> productIds);

}
