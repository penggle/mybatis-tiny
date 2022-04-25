package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductSaleSpec;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品销售规格Mapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface ProductSaleSpecMapper extends BaseEntityMapper<ProductSaleSpec> {
}
