package com.siiri.limiter.core.aspect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * @author van
 */
public class RateLimitAspectConfig {
    private RateLimitAspectConfig () {}

    /**
     * IP — rateLimit , 用以区分不同groupName之间的限流措施
     * 可以暂时不考虑 @Beta 问题,考虑项目进度先采用令牌桶方案,后续考虑切换滑动窗口限流方案
     */
    protected static Map<String, Map<String, RateLimiter>> rateLimiterMap = Maps.newConcurrentMap();

    /**
     * 用于 {@link com.siiri.limiter.core.annotation.EnableIpLimit} 自动扫描
     * @return aspect
     */
    @Bean
    public IpLimitAspect aspect() {
        return new IpLimitAspect();
    }
}
