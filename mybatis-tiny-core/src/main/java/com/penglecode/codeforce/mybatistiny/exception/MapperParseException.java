package com.penglecode.codeforce.mybatistiny.exception;

/**
 * Mybatis XML-Mapper解析异常
 *
 * @author pengpeng
 * @version 1.0
 */
public class MapperParseException extends RuntimeException {

    public MapperParseException(String message) {
        super(message);
    }

    public MapperParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapperParseException(Throwable cause) {
        super(cause);
    }

}
