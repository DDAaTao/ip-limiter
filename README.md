~~~ 
author: van , ggfanwentao@gmail.com
~~~
---

# Ip-Limit: 轻量级注解式IP限流组件

## 项目简介
基于JVM缓存的轻量级、注解式IP限流组件，方便项目快速引用，满足多线程场景。

**使用样例**
https://github.com/DDAaTao/ip-limiter-example

**Ip-Limit 具有以下特性:**
- 基于注解使用，简单快捷，可添加到Controller类上，也可以添加到具体的API方法上
- 业务入侵小，不用过多配置类，但可以支持多种场景配置
- 实现组级别统一限流，即可满足单接口单组场景，又可满足多接口单组
- 可以通过配置文件批量黑白名单，满足单产品多项目的动态配置需求
eg. 
```properties
# 配置文件中配置,需要注意分隔符为半角的','
my.white.ip.list=172.16.50.21,172.16.50.22,172.16.50.23
```
```
// 代码中使用时
@IpLimit(limitType = LimitType.WHITE_LIST, whiteList = "${my.white.ip.list}")
// 或
@IpLimit(limitType = LimitType.WHITE_LIST, whiteList = {"${my.white.ip.list}","172.16.50.35"})
```
- 根据limitType支持多种模式限流
  - 默认根据单位次数/单位时间进行限流
  - 基于黑、白名单进行限流
  - 黑白名单 + 单位次数/单位时间进行限流
- 黑白名单IP规则实现多种模糊模式配置
  - 172.\*.\*.1
  - 172.*.1
  - 172.*
  - *.21
  - \*

**核心限流模式 - LimitType类**
- DEFAULT - 走默认限流策略,不考虑黑白名单参数
- WHITE_LIST - 只考虑白名单策略,非白名单的请求全部回绝
- BLACK_LIST - 只考虑黑名单策略,非黑名单请求不做限流措施
- DEFAULT_WITH_WHITE_LIST - 在默认限流策略的基础上,白名单内的IP不做限流
- DEFAULT_WITH_BLACK_LIST - 在默认限流策略的基础上,直接403黑名单
- DEFAULT_WITH_WHITE_AND_BLACK_LIST - 在默认限流策略的基础上,直接403黑名单,再让白名单内的IP直接同行

**Ip-Limit 计划实现功能:**
- 对应组黑白名单只需单次配置即可作用于同组下的其他接口
  - 目前的黑白名单配置是按第一次请求时缓存到rateLimiterMap中的RateLimiter进行限流的
- 通过RateLimitAspectConfig添加统一配置组、限流、黑白名单功能
- 可将IP更换为指定字段（比如账号）限流
- 通过限流类型完善内部请求防止误伤场景
- 黑白名单配置可更换为动态数据源 
- 黑名单可以根据限流规则动态增删 
- IP缓存统计数据可更换其他存储数据源，避免过多占用JVM缓存
- 可动态配置请求拒绝决策，方便上层进行捕捉和异常处理
- 用户自定义限流器
- 全局限流、全局分IP限流


## 快速开始

1. 引入Ip-Limit依赖（已发布至Maven中央仓库）
```xml
  <!-- 建议使用最新版本{ip-limiter.version} -->
  <dependency>
    <groupId>io.github.DDAaTao</groupId>
    <artifactId>ip-limiter</artifactId>
    <version>1.0.2</version>
  </dependency>
```
2. 将 @EnableIpLimit 添加到 webApplication 类上,或其他可以被 Spring 扫描到的类上
3. 将 @IpLimit 注解添加到想要做IP限流的方法（接口）上，根据需求动态调整参数

> 如果项目中没有引入guava、spring-context包,则需要手动引入,否则会报java.lang.NoSuchMethodError异常
> 
> 从1.0.1开始默认引入,如果项目中已有相关依赖,可以考虑通过<exclusions>去除掉

## 最佳实践
### 一、自定义限流异常处理机制
```Java
/**
 * 默认情况下,当请求超出限流限制时,会打印日志并抛出 IpLimitException 异常
 * 用户可以通过统一异常拦截器捕获并自定义业务处理
 * 后续考虑增加回调或钩子方法
 * */
@Slf4j
@ControllerAdvice
public class BaseExceptionHandler {

  @ExceptionHandler(IpLimitException.class)
  @ResponseBody
  public RestApiResult<Object> resolveCommonException(IpLimitException e) {
    log.error("IpLimitException Intercept. Please try again later.. ");
    return RestApiResult.fail("IpLimitException Intercept. Please try again later.. ");
  }
  
}

```
### 二、已存在鉴权方案时的接入方案
```
SpringCloud 项目或者大部分项目一般都会有默认鉴权机制，比如Spring-Security。
这个时候如果有需要和外部对接的接口，有两种处理方法，一个是通过类似Oauth2之类的三方协议处理，
但是流程对接较为麻烦，尤其是有些内外项目，本身已有较好的安全保证。此时就可以另外一种方式，也就是`白名单`来处理
也就是 LimitType.WHITE_LIST
```


## 异常记录
1. 暂时不支持Spring-6.x