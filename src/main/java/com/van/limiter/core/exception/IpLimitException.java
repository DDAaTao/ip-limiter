package com.van.limiter.core.exception;



/**
 * 专用于Ip限流异常,可用于自定义捕获
 * @author van
 */
public class IpLimitException extends RuntimeException {

    private static final long serialVersionUID = 8669822979975640792L;

    /**
     * 限流异常时触发的Ip
     */
    private final String requestIp;
    /**
     * 异常接口的GroupName
     */
    private final String groupName;

    public IpLimitException(String message, String requestIp, String groupName) {
        super(message);
        this.requestIp = requestIp;
        this.groupName = groupName;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public String getGroupName() {
        return groupName;
    }
}
