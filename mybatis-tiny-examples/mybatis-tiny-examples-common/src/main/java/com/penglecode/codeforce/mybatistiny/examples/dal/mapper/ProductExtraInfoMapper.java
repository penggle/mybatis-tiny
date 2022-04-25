package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.ProductExtraInfo;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品额外信息Mapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface ProductExtraInfoMapper extends BaseEntityMapper<ProductExtraInfo> {
}
