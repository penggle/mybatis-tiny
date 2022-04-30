package com.penglecode.codeforce.mybatistiny.examples.test;

import com.penglecode.codeforce.mybatistiny.examples.config.MybatisConfiguration;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.StudentEnhancedMapper;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.StudentMapper;
import com.penglecode.codeforce.mybatistiny.examples.dal.mapper.StudentMysqlMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author pengpeng
 * @version 1.0
 */
@SpringJUnitConfig(MybatisConfiguration.class)
public class CustomMapperTestBySpring extends StudentTestCase {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private StudentEnhancedMapper studentEnhancedMapper;

    @Autowired
    private StudentMysqlMapper studentMysqlMapper;

    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

    protected <T> void doInTransaction(ExampleExecutor executor) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
        transactionTemplate.execute(status -> {
            executor.execute();
            return null;
        });
    }

    /**
     * 创建测试数据
     */
    @Test
    public void insertStudentTest() {
        doInTransaction(this::insertStudent);
    }

    /**
     * 测试标准merge-into语句
     * 但是当前是MySQL数据库，肯定会报错，该测试目的只是看看动态SQL如何
     */
    @Test
    public void mergeStudentTest1() {
        doInTransaction(() -> mergeStudent(studentEnhancedMapper));
    }

    /**
     * 测试MySQL版merge-into语句
     */
    @Test
    public void mergeStudentTest2() {
        doInTransaction(() -> mergeStudent(studentMysqlMapper));
    }

    /**
     * 测试在基础的BaseEntityMapper上调用updateByCriteria()方法
     */
    @Test
    public void updateStudentTest1() {
        doInTransaction(() -> updateStudent(studentMapper));
    }

    /**
     * 测试在自定义的EnhancedBaseMapper上调用updateByCriteria()方法
     */
    @Test
    public void updateStudentTest2() {
        doInTransaction(() -> updateStudent(studentEnhancedMapper));
    }

    /**
     * 测试在自定义的MysqlBaseMapper上扩展的replace()方法
     */
    @Test
    public void replaceStudentTest() {
        doInTransaction(this::replaceStudent);
    }

    /**
     * 测试StudentEnhancedMapper上的自定义方法
     */
    @Test
    public void selectAllStudentsTest() {
        selectAllStudents();
    }

    @Override
    public StudentMapper getStudentMapper() {
        return studentMapper;
    }

    @Override
    public StudentEnhancedMapper getStudentEnhancedMapper() {
        return studentEnhancedMapper;
    }

    @Override
    public StudentMysqlMapper getStudentMysqlMapper() {
        return studentMysqlMapper;
    }

}
