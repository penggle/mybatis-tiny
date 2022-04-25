package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductPriceStat;
import com.penglecode.codeforce.mybatistiny.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品统计Mapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface ProductStatisticsMapper extends BaseMapper {

    /**
     * 根据ids获取商品价格统计信息
     * @param ids
     * @return
     */
    List<ProductPriceStat> selectPriceStatListByIds(List<Long> ids);

}
