package com.penglecode.codeforce.mybatistiny.examples.domain.model;

import com.penglecode.codeforce.common.domain.EntityObject;
import com.penglecode.codeforce.mybatistiny.annotations.Column;
import com.penglecode.codeforce.mybatistiny.annotations.GenerationType;
import com.penglecode.codeforce.mybatistiny.annotations.Id;
import com.penglecode.codeforce.mybatistiny.annotations.Table;

import java.time.LocalDateTime;

/**
 * 学生实体对象
 *
 * @author pengpeng
 * @version 1.0
 */
@Table("t_student")
public class Student implements EntityObject {

    private static final long serialVersionUID = 1L;

    /** ID */
    @Id(strategy=GenerationType.IDENTITY)
    private Long id;

    /** 学号 */
    private String code;

    /** 姓名 */
    private String name;

    /** 性别 */
    private Character sex;

    /** 年龄 */
    private Integer age;

    @Column(updatable=false)
    private LocalDateTime created;

    private LocalDateTime updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character getSex() {
        return sex;
    }

    public void setSex(Character sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    @Override
    public Long identity() {
        return id;
    }

}
