package com.penglecode.codeforce.mybatistiny.exception;

/**
 * BaseEntityMapper freemarker模板解析渲染异常
 *
 * @author pengpeng
 * @version 1.0
 */
public class MapperTemplateException extends RuntimeException {

    public MapperTemplateException(String message) {
        super(message);
    }

    public MapperTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapperTemplateException(Throwable cause) {
        super(cause);
    }

}
