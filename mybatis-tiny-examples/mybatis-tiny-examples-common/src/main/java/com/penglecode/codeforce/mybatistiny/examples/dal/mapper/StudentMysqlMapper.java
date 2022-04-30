package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.Student;
import com.penglecode.codeforce.mybatistiny.examples.extensions.MysqlBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface StudentMysqlMapper extends MysqlBaseMapper<Student> {
}
