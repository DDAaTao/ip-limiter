package com.van.limiter.core.enums;

/**
 * 限流规则
 * @author van
 */
public enum LimitType {
    /**
     * 走默认限流策略,不考虑黑白名单参数
     */
    DEFAULT,
    /**
     * 只考虑白名单策略,非白名单的请求全部回绝
     */
    WHITE_LIST,
    /**
     * 只考虑黑名单策略,非黑名单请求不做限流措施
     */
    BLACK_LIST,
    /**
     * 在默认限流策略的基础上,白名单内的IP不做限流
     */
    DEFAULT_WITH_WHITE_LIST,
    /**
     * 在默认限流策略的基础上,直接403黑名单
     */
    DEFAULT_WITH_BLACK_LIST,
    /**
     * 在默认限流策略的基础上,直接403黑名单,再让白名单内的IP直接同行
     */
    DEFAULT_WITH_WHITE_AND_BLACK_LIST
}
