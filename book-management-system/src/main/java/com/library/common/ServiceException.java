package com.library.common;

/**
 * 业务异常，用于把错误信息传递给页面展示
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }
}
