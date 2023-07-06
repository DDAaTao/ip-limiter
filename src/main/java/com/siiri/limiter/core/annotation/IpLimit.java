package com.siiri.limiter.core.annotation;

import com.siiri.limiter.core.constant.IpLimitConstant;
import com.siiri.limiter.core.enums.CurrentLimiterType;
import com.siiri.limiter.core.enums.LimitTimeType;
import com.siiri.limiter.core.enums.LimitType;

import java.lang.annotation.*;

/**
 * 限流具体注解
 * @author van
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IpLimit {

    /**
     * 限流器类型,默认采用滑动窗口限流器,可配置为令牌桶模式
     * todo(van) 可用户自行实现限流器
     * @return 限流器类型
     */
    CurrentLimiterType currentLimiter() default CurrentLimiterType.SLIDING_WINDOW;

    /**
     * 限流组名称,用以分组限流,注解提供默认,该属性可自行维护
     * 限流策略为IP下,对应分组分开进行限流统计,如果不需要分组则使用默认即可
     * @return 限流组名称
     */
    String groupName() default IpLimitConstant.COMMON_LIMIT_GROUP;

    /**
     * 限流策略类型,策略类型同一Group下应当维护同一种
     * DEFAULT 走默认限流策略,不考虑黑白名单参数
     * LimitType.WHITE_LIST 只考虑白名单策略,非白名单的请求全部回绝
     * LimitType.BLACK_LIST 只考虑黑名单策略,非黑名单请求不做限流措施
     * LimitType.DEFAULT_WITH_WHITE_LIST 在默认限流策略的基础上,白名单内的IP不做限流
     * LimitType.DEFAULT_WITH_BLACK_LIST 在默认限流策略的基础上,直接403黑名单
     * LimitType.DEFAULT_WITH_WHITE_AND_BLACK_LIST 在默认限流策略的基础上,直接403黑名单,再让白名单内的IP直接同行
     * @return 限流策略类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流时间单位类型
     * @return 毫秒,秒,分钟
     */
    LimitTimeType limitTimeType() default LimitTimeType.SECOND;

    /**
     * 限流的单位时间长度
     * @return 时间长度,单位由 {@link IpLimit#limitTimeType()} 决定
     */
    long unitTime() default 1;

    /**
     * 限流单位时间长度内的最多次数
     * @return 最多次数
     */
    double maxTimes() default 10;

    /**
     * 白名单,使用方式由 limitType() 确定,比如选择默认LimitType.DEFAULT时该参数配置无用
     * @return 白名单str
     */
    String[] whiteList() default {"localhost", "127.0.0.1"};

    /**
     * 黑名单,使用方式由 limitType() 确定,比如选择默认LimitType.DEFAULT时该参数配置无用
     * @return 黑名单list
     */
    String[] blackList() default {};
}