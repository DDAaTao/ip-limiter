package com.van.limiter.core.aspect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.van.limiter.core.annotation.IpLimit;
import com.van.limiter.core.exception.IpLimitException;
import com.van.limiter.core.util.IpLimitUtils;
import com.van.limiter.core.util.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author van
 */
@Aspect
@Component
public class IpLimitAspect {

    @Autowired
    private IpLimitUtils ipLimitUtils;

    @Pointcut("@within(com.van.limiter.core.annotation.IpLimit) || @annotation(com.van.limiter.core.annotation.IpLimit)")
    private void pointMethod() {
    }

    @Around("pointMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;

        IpLimit ipLimitAnnotation = null;
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            ipLimitAnnotation = methodSignature.getMethod().getAnnotation(IpLimit.class);
            if (ipLimitAnnotation == null) {
                ipLimitAnnotation = methodSignature.getMethod().getDeclaringClass().getAnnotation(IpLimit.class);
            }
        }
        assert ipLimitAnnotation != null;

        HttpServletRequest request = attributes.getRequest();
        String requestHost = IpUtils.getIpAddress(request);
        double permitsPerSecond = computePermitsPerSecond(ipLimitAnnotation);
        switch (ipLimitAnnotation.limitType()) {
            case DEFAULT:
                currentLimiterSwitch(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case WHITE_LIST:
                // 如果是白名单内的,则不再进行校验
                if (Boolean.TRUE.equals(ipLimitUtils.ipInWhiteIpList(ipLimitAnnotation, requestHost))) {
                    return joinPoint.proceed();
                }
                ipLimitError(ipLimitAnnotation, requestHost);
                break;
            case BLACK_LIST:
                // 如果存在于黑名单,则抛出异常
                if (Boolean.TRUE.equals(ipLimitUtils.ipInBlackIpList(ipLimitAnnotation, requestHost))) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
                return joinPoint.proceed();
            case DEFAULT_WITH_WHITE_LIST:
                if (Boolean.TRUE.equals(ipLimitUtils.ipInWhiteIpList(ipLimitAnnotation, requestHost))) {
                    return joinPoint.proceed();
                }
                currentLimiterSwitch(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case DEFAULT_WITH_BLACK_LIST:
                if (Boolean.TRUE.equals(ipLimitUtils.ipInBlackIpList(ipLimitAnnotation, requestHost))) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
                currentLimiterSwitch(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case DEFAULT_WITH_WHITE_AND_BLACK_LIST:
                if (Boolean.TRUE.equals(ipLimitUtils.ipInBlackIpList(ipLimitAnnotation, requestHost))) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
                if (Boolean.TRUE.equals(ipLimitUtils.ipInWhiteIpList(ipLimitAnnotation, requestHost))) {
                    return joinPoint.proceed();
                }
                currentLimiterSwitch(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            default:break;
        }
        return joinPoint.proceed();
    }

    /**
     * 用于统一异常处理
     * @param ipLimitAnnotation ipLimitAnnotation
     * @param requestHost 请求方IP
     */
    private void ipLimitError(IpLimit ipLimitAnnotation, String requestHost) {
        throw new IpLimitException(String.format("Ip limiter warning ! IP: %s, GroupName: %s", requestHost, ipLimitAnnotation.groupName()),
                requestHost, ipLimitAnnotation.groupName(), ipLimitAnnotation);
    }

    /**
     * 限流器选择入口方法
     * @param ipLimitAnnotation 用于获取分组等信息数据
     * @param requestHost 请求方IP
     * @param permitsPerSecond 计算后的每秒允许请求数量
     */
    private void currentLimiterSwitch(IpLimit ipLimitAnnotation, String requestHost, double permitsPerSecond) {
        switch (ipLimitAnnotation.currentLimiter()) {
            case TOKEN_BUCKET:
                tokenBucketLimitMethod(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case SLIDING_WINDOW:
                windowLimitMethod(ipLimitAnnotation, requestHost);
                break;
            default:break;
        }
    }

    /**
     * 令牌桶限流核心逻辑
     * @param ipLimitAnnotation 用于获取分组等信息数据
     * @param requestHost 请求方IP
     * @param permitsPerSecond 计算后的每秒允许请求数量
     */
    @SuppressWarnings("ALL")
    private void tokenBucketLimitMethod(IpLimit ipLimitAnnotation, String requestHost, double permitsPerSecond) {
        // 取并判断是否已记录该IP的限流Map
        Map<String, RateLimiter> rateLimiterMap = RateLimitAspectConfig.TOKEN_BUCKET_LIMITER_MAP.computeIfAbsent(requestHost, k -> {
            RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);
            Map<String, RateLimiter> stringRateLimiterMap = Maps.newConcurrentMap();
            stringRateLimiterMap.put(ipLimitAnnotation.groupName(), rateLimiter);
            return stringRateLimiterMap;
        });
        // 取并判断是否已记录该IP的对应Group限流情况
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(ipLimitAnnotation.groupName(), k -> {
            RateLimiter thisRateLimiter = RateLimiter.create(permitsPerSecond);
            rateLimiterMap.put(ipLimitAnnotation.groupName(), thisRateLimiter);
            return thisRateLimiter;
        });

        // 判断是否可以获取令牌成功,否则报异常
        if (Boolean.FALSE.equals(rateLimiter.tryAcquire())) {
            ipLimitError(ipLimitAnnotation, requestHost);
        }
    }

    /**
     * 滑动窗口限流核心逻辑
     * @param ipLimitAnnotation 用于获取分组等信息数据
     * @param requestHost 请求方IP
     */
    private void windowLimitMethod(IpLimit ipLimitAnnotation, String requestHost) {
        Map<String, Deque<LocalDateTime>> stringDequeMap = RateLimitAspectConfig.WINDOW_TIMESTAMP_LIMITER_MAP
                .computeIfAbsent(requestHost, k -> {
                    Map<String, Deque<LocalDateTime>> dequeMap =  Maps.newConcurrentMap();
                    dequeMap.put(ipLimitAnnotation.groupName(), new ConcurrentLinkedDeque<>());
                    return dequeMap;
                });

        // 获取滑动窗口的时间窗时间类型,默认为毫秒
        TemporalUnit temporalUnit;
        switch (ipLimitAnnotation.limitTimeType()) {
            case SECOND:
                temporalUnit = ChronoUnit.SECONDS;
                break;
            case MINUTE:
                temporalUnit = ChronoUnit.MINUTES;
                break;
            default:temporalUnit = ChronoUnit.MILLIS;
        }

        // 丢弃超出滑动窗口的失效数据
        Deque<LocalDateTime> dateTimeDeque = stringDequeMap.get(ipLimitAnnotation.groupName());
        while (!dateTimeDeque.isEmpty() && LocalDateTime.now().minus(ipLimitAnnotation.unitTime(), temporalUnit).isAfter(dateTimeDeque.peekFirst())) {
            dateTimeDeque.pollFirst();
        }

        // 超出最大请求次数报出异常
        if (dateTimeDeque.size() > ipLimitAnnotation.maxTimes()) {
            ipLimitError(ipLimitAnnotation, requestHost);
        }

        // 加入当前请求
        dateTimeDeque.push(LocalDateTime.now());
    }


    /**
     * 将不同限流时间单位转化为秒级限流措施
     * @param ipLimit ipLimit
     * @return 每秒允许通过请求
     */
    private double computePermitsPerSecond(IpLimit ipLimit) {
        switch (ipLimit.limitTimeType()) {
            case MILLISECOND:
                return ipLimit.maxTimes() * 1000 / ipLimit.unitTime() ;
            case SECOND:
                return ipLimit.maxTimes() / ipLimit.unitTime();
            case MINUTE:
                return ipLimit.maxTimes() / (ipLimit.unitTime() * 60);
            default:
                return ipLimit.maxTimes();
        }
    }

}
