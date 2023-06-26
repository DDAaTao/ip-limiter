package com.siiri.limiter.core.annotation;

import com.siiri.limiter.core.constant.IpLimitConstant;
import com.siiri.limiter.core.enums.LimitTimeType;
import com.siiri.limiter.core.enums.LimitType;

import java.lang.annotation.*;

/**
 * @author van
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IpLimit {
    /**
     * 限流组名称,用以分组限流
     * @return 限流组名称
     */
    String groupName() default IpLimitConstant.COMMON_LIMIT_GROUP;

    /**
     * 限流策略类型
     * @return
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流时间单位类型
     * @return 秒,分钟
     */
    LimitTimeType limitTimeType() default LimitTimeType.SECOND;

    /**
     * 限流的单位时间长度
     * @return 时间长度
     */
    long unitTime() default 1;

    /**
     * 限流单位时间长度内的最多次数
     * @return 最多次数
     */
    double maxTimes() default 10;

    /**
     * 白名单, 使用方式由 limitType() 确定
     * @return 白名单str
     */
    String[] whiteList() default {"localhost", "127.0.0.1"};

    /**
     * 黑名单, 使用方式由 limitType() 确定
     * @return 黑名单list
     */
    String[] blackList() default {};

}