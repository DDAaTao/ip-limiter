package com.van.limiter.core.aspect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.van.limiter.core.annotation.EnableIpLimit;
import com.van.limiter.core.util.IpLimitUtils;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author van
 */
public class RateLimitAspectConfig {
    private RateLimitAspectConfig () {}
    /**
     * 存储全局请求时间队列
     */
    protected static final Deque<LocalDateTime> GLOBAL_REQUEST_TIMESTAMP_QUEUE = new ConcurrentLinkedDeque<>();

    /**
     * 令牌桶模式，[IP,[Group,RateLimit-令牌桶]] , 用以区分不同groupName之间的限流措施
     * 可以暂时不考虑 @Beta 问题,考虑项目进度先采用令牌桶方案,后续考虑切换滑动窗口限流方案
     */
    @SuppressWarnings("ALL")
    protected static final Map<String, Map<String, RateLimiter>> TOKEN_BUCKET_LIMITER_MAP = Maps.newConcurrentMap();


    /**
     * 滑动窗口模式，[IP,[Group,RateLimit]], 用以区分不同groupName之间的限流措施
     */
    protected static final Map<String, Map<String, Deque<LocalDateTime>>> WINDOW_TIMESTAMP_LIMITER_MAP = Maps.newConcurrentMap();

    /**
     * 用于 {@link EnableIpLimit} 自动扫描
     * @return aspect
     */
    @Bean
    public IpLimitAspect aspect() {
        return new IpLimitAspect();
    }

    /**
     * 用于开放给用户一些内部功能
     * @return IpLimitUtils
     */
    @Bean
    public IpLimitUtils utils() {
        return new IpLimitUtils();
    }
}
