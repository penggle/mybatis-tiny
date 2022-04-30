package com.penglecode.codeforce.mybatistiny.examples.dal.mapper;

import com.penglecode.codeforce.mybatistiny.examples.domain.model.Student;
import com.penglecode.codeforce.mybatistiny.examples.extensions.EnhancedBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 测试自定义的EnhancedBaseMapper
 *
 * @author pengpeng
 * @version 1.0
 */
@Mapper
public interface StudentEnhancedMapper extends EnhancedBaseMapper<Student> {

    List<Student> selectAllStudents();

}
