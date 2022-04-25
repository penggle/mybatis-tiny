package com.penglecode.codeforce.mybatistiny.annotations;

/**
 * 主键生成策略
 *
 * @author pengpeng
 * @version 1.0
 */
public enum GenerationType {

    /**
     * 序列，例如Oracle数据库
     */
    SEQUENCE, 

    /**
     * 自增，例如MySQL、DB2、SQLServer、PG
     */
    IDENTITY, 

    /**
     * 由客户端程序自己生成并在插入之前设置好
     */
    NONE

}