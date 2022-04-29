package com.penglecode.codeforce.mybatistiny.exception;

/**
 * Mybatis XML-Mapper解析异常
 *
 * @author pengpeng
 * @version 1.0
 */
public class MapperXmlParseException extends RuntimeException {

    public MapperXmlParseException(String message) {
        super(message);
    }

    public MapperXmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapperXmlParseException(Throwable cause) {
        super(cause);
    }

}
