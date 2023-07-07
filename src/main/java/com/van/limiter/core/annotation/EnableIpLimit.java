package com.van.limiter.core.annotation;

import com.van.limiter.core.aspect.RateLimitAspectConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于自动注册,使用时将该注解添加到 application 类上即可
 * @author van
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RateLimitAspectConfig.class)
public @interface EnableIpLimit {
}
