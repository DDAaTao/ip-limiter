package com.siiri.limiter.core.enums;

/**
 * 限流器类型
 * @author van
 */
public enum CurrentLimiterType {
    /**
     * 滑动窗口
     */
    SLIDING_WINDOW,
    /**
     * 令牌桶
     */
    TOKEN_BUCKET;

}
