package com.penglecode.codeforce.mybatistiny.examples.util;

/**
 * @author pengpeng
 * @version 1.0
 */
public interface StudentMapper extends CustomBaseMapper<Student> {

    int selectStudentByName(String studentName);

}
