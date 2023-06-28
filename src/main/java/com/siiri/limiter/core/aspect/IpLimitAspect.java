package com.siiri.limiter.core.aspect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.siiri.limiter.core.annotation.IpLimit;
import com.siiri.limiter.core.constant.IpLimitConstant;
import com.siiri.limiter.core.exception.IpLimitException;
import com.siiri.limiter.core.util.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author van
 */
@Aspect
@Component
public class IpLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(IpLimitAspect.class);

    @Autowired
    private Environment environment;

    @Pointcut("@annotation(com.siiri.limiter.core.annotation.IpLimit)")
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
        }
        assert ipLimitAnnotation != null;

        HttpServletRequest request = attributes.getRequest();
        String requestHost = IpUtils.getIpAddress(request);
        log.debug("Limiter find request IP: {}", requestHost);
        double permitsPerSecond = computePermitsPerSecond(ipLimitAnnotation);
        switch (ipLimitAnnotation.limitType()) {
            case DEFAULT:
                defaultLimitMethod(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case WHITE_LIST:
                // 如果是白名单内的,则不再进行校验
                if (Boolean.TRUE.equals(strInIpArray(ipLimitAnnotation.whiteList(), requestHost))) {
                    return joinPoint.proceed();
                }
                ipLimitError(ipLimitAnnotation, requestHost);
                break;
            case BLACK_LIST:
                // 如果存在于黑名单,则抛出异常
                if (Boolean.TRUE.equals(strInIpArray(ipLimitAnnotation.blackList(), requestHost))) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
                return joinPoint.proceed();
            case DEFAULT_WITH_WHITE_LIST:
                if (Boolean.TRUE.equals(strInIpArray(ipLimitAnnotation.whiteList(), requestHost))) {
                    return joinPoint.proceed();
                }
                defaultLimitMethod(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case DEFAULT_WITH_BLACK_LIST:
                if (Boolean.TRUE.equals(strInIpArray(ipLimitAnnotation.blackList(), requestHost))) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
                defaultLimitMethod(ipLimitAnnotation, requestHost, permitsPerSecond);
                break;
            case DEFAULT_WITH_WHITE_AND_BLACK_LIST:
                if (Boolean.TRUE.equals(strInIpArray(ipLimitAnnotation.blackList(), requestHost))) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
                if (Boolean.TRUE.equals(strInIpArray(ipLimitAnnotation.whiteList(), requestHost))) {
                    return joinPoint.proceed();
                }
                defaultLimitMethod(ipLimitAnnotation, requestHost, permitsPerSecond);
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
        log.warn("Ip limiter warning ! IP: {}, GroupName: {}", requestHost, ipLimitAnnotation.groupName());
        throw new IpLimitException("Limiter warning !");
    }

    /**
     * 令牌桶限流核心逻辑
     * @param ipLimitAnnotation 用于获取分组等信息数据
     * @param requestHost 请求方IP
     * @param permitsPerSecond 计算后的每秒允许请求数量
     */
    private void defaultLimitMethod(IpLimit ipLimitAnnotation, String requestHost, double permitsPerSecond) {
        Map<String, RateLimiter> stringRateLimiterMap = RateLimitAspectConfig.rateLimiterMap.get(requestHost);
        if (CollectionUtils.isEmpty(stringRateLimiterMap)) {
            RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);
            rateLimiter.acquire();
            stringRateLimiterMap = Maps.newConcurrentMap();
            stringRateLimiterMap.put(ipLimitAnnotation.groupName(), rateLimiter);
            RateLimitAspectConfig.rateLimiterMap.put(requestHost, stringRateLimiterMap);
        } else {
            RateLimiter rateLimiter = stringRateLimiterMap.get(ipLimitAnnotation.groupName());
            if (rateLimiter != null) {
                if (Boolean.FALSE.equals(rateLimiter.tryAcquire())) {
                    ipLimitError(ipLimitAnnotation, requestHost);
                }
            } else {
                rateLimiter = RateLimiter.create(permitsPerSecond);
                rateLimiter.acquire();
                stringRateLimiterMap = Maps.newConcurrentMap();
                stringRateLimiterMap.put(ipLimitAnnotation.groupName(), rateLimiter);
            }
        }
    }

    /**
     * 将不同限流时间单位转化为秒级限流措施
     * @param ipLimit ipLimit
     * @return 每秒允许通过请求
     */
    private double computePermitsPerSecond(IpLimit ipLimit) {
        switch (ipLimit.limitTimeType()) {
            case SECOND:
                return ipLimit.maxTimes() / ipLimit.unitTime();
            case MINUTE:
                return ipLimit.maxTimes() / (ipLimit.unitTime() * 60);
            default:
                return ipLimit.maxTimes();
        }
    }

    /**
     * 判断对应字符串是否存在于数组内
     * @param array array
     * @param str 字符串
     * @return boolean
     */
    private boolean strInIpArray(String[] array, String str) {
        for (String arrayStr : array) {
            arrayStr = environment.resolvePlaceholders(arrayStr);
            String[] split = arrayStr.split(IpLimitConstant.IP_PROPERTIES_SPLIT);
            for (String s : split) {
                if (IpUtils.ipFuzzyMatch(s.trim(), str)) {
                    return true;
                }
            }
        }
        return false;
    }
}
