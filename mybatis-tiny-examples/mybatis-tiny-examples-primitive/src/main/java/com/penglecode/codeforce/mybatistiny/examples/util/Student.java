package com.penglecode.codeforce.mybatistiny.examples.util;

import com.penglecode.codeforce.common.domain.EntityObject;

/**
 * @author pengpeng
 * @version 1.0
 */
public class Student implements EntityObject {

    private Long studentId;

    private String studentName;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

}
