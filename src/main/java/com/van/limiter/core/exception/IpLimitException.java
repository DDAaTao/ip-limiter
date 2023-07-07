package com.van.limiter.core.exception;


/**
 * 专用于Ip限流异常,可用于自定义捕获
 * @author van
 */
public class IpLimitException extends RuntimeException {

    private static final long serialVersionUID = 8669822979975640792L;

    public IpLimitException(String message) {
        super(message);
    }
}
