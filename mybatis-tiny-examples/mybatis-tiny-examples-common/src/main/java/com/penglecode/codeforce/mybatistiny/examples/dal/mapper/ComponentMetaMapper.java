package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.ComponentMeta;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组件元信息Mapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface ComponentMetaMapper extends BaseEntityMapper<ComponentMeta> {
}
