package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductBaseInfo;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 商品基础信息Mapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface ProductBaseInfoMapper extends BaseEntityMapper<ProductBaseInfo> {

    /**
     * 常规Mybatis动态条件查询使用示例
     * 以及自动分页示例（这里自动分页不包括查COUNT）
     *
     * @param condition
     * @param rowBounds
     * @return
     */
    List<ProductBaseInfo> selectProductsByCondition(ProductBaseInfo condition, RowBounds rowBounds);

}
