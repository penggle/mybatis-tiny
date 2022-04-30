package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.common.support.MapLambdaBuilder;
import com.penglecode.codeforce.common.util.CollectionUtils;
import com.penglecode.codeforce.common.util.JsonUtils;
import com.penglecode.codeforce.mybatistiny.dsl.LambdaQueryCriteria;
import com.penglecode.codeforce.mybatistiny.dsl.QueryColumns;
import com.penglecode.codeforce.mybatistiny.dsl.QueryCriteria;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.StudentEnhancedMapper;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.StudentMapper;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.StudentMysqlMapper;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.Student;
import com.penglecode.codeforce.mybatistiny.examples.extensions.EnhancedBaseMapper;
import com.penglecode.codeforce.mybatistiny.mapper.BaseEntityMapper;
import sun.plugin2.message.GetAppletMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author pengpeng
 * @version 1.0
 */
public abstract class StudentTestCase {

    protected void insertStudent() {
        Student student = new Student();
        student.setCode("002");
        student.setName("李公公");
        student.setSex('男');
        student.setAge(16);
        student.setCreated(LocalDateTime.now());
        student.setUpdated(student.getCreated());
        getStudentMapper().insert(student);
    }

    protected void mergeStudent(EnhancedBaseMapper<Student> enhancedStudentMapper) {
        Student student = new Student();
        student.setCode("001");
        student.setName("王小小");
        student.setSex('女');
        student.setAge(18);
        student.setCreated(LocalDateTime.now());
        student.setUpdated(student.getCreated());
        enhancedStudentMapper.merge(student, new QueryColumns(Student::getName, Student::getSex, Student::getAge, Student::getUpdated));
    }

    protected void updateStudent(BaseEntityMapper<Student> studentMapper) {
        Student student = new Student();
        student.setCode("001");
        student.setName("王小小");
        student.setSex('女');
        student.setAge(18);
        student.setCreated(LocalDateTime.now());
        student.setUpdated(student.getCreated());
        Map<String,Object> updateColumns = MapLambdaBuilder.of(student)
                    .with(Student::getName)
                    .with(Student::getSex)
                    .with(Student::getAge)
                    .with(Student::getUpdated)
                    .build();
        QueryCriteria<Student> updateCriteria = LambdaQueryCriteria.of(student)
                    .eq(Student::getCode);
        studentMapper.updateByCriteria(updateCriteria, updateColumns);
    }

    protected void replaceStudent() {
        Student student = new Student();
        student.setCode("001");
        student.setName("王小小");
        student.setSex('女');
        student.setAge(18);
        student.setCreated(LocalDateTime.now());
        student.setUpdated(student.getCreated());
        getStudentMysqlMapper().replace(student);
    }

    protected void selectAllStudents() {
        List<Student> allStudents = getStudentEnhancedMapper().selectAllStudents();
        if(!CollectionUtils.isEmpty(allStudents)) {
            allStudents.forEach(student -> System.out.println(JsonUtils.object2Json(student)));
        }
    }

    public abstract StudentMapper getStudentMapper();

    public abstract StudentEnhancedMapper getStudentEnhancedMapper();

    public abstract StudentMysqlMapper getStudentMysqlMapper();

}
