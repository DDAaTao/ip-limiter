~~~ 
author: van , ggfanwentao@gmail.com
~~~
English | [点击跳转](README.md)

---

# Ip-Limiter: 轻量级注解式IP限流组件

## 项目简介
基于JVM缓存的轻量级、注解式IP限流组件，方便项目快速引用，满足多线程场景。

### 使用样例
> 包含较为详细的演示使用代码

项目地址： https://github.com/DDAaTao/ip-limiter-example
项目国内地址: https://gitee.com/fanwentaomayun/ip-limiter-example

**Ip-Limiter 具有以下特性:**
- 基于注解使用，简单快捷，可添加到Controller类上，也可以添加到具体的API方法上
- 业务入侵小，不用过多配置类，但可以支持多种场景配置
- 实现组级别统一限流，即可满足单接口单组场景，又可满足多接口单组
- 支持配置文件配置、外部动态配置（新增、删除）黑白名单

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

- 黑白名单IP规则实现多种模糊模式配置，支持IPv6
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

## 快速开始

1. 引入Ip-Limit依赖（已发布至Maven中央仓库）
```xml
  <!-- 建议使用最新版本{ip-limiter.version} -->
  <dependency>
    <groupId>io.github.DDAaTao</groupId>
    <artifactId>ip-limiter</artifactId>
    <version>1.0.3</version>
  </dependency>
```
2. 将 @EnableIpLimit 添加到 webApplication 类上,或其他可以被 Spring 扫描到的类上
3. 将 @IpLimit 注解添加到想要做IP限流的方法（接口）上，根据需求动态调整参数

> 如果项目中没有引入guava、spring-context包,则需要手动引入,否则会报java.lang.NoSuchMethodError异常
>
> 从1.0.1开始默认引入,如果项目中已有相关依赖,可以考虑通过exclusions去除掉

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
    log.error("IpLimitException Intercept. Please try again later.. " + e.getMessage());
    // 此处可以通过 e.getRequestIp() 和 e.getGroupName() 做一些限流回调业务处理
    return RestApiResult.fail("IpLimitException Intercept. Please try again later.. ");
  }
  
}

```
### 二、已存在鉴权方案时的接入方案

SpringCloud 项目或者大部分项目一般都会有做自己的鉴权机制，比如Spring-Security。
这个时候如果有需要和外部对接的接口，有两种处理方法，一个是通过类似Oauth2之类的三方协议处理，
但是流程对接较为麻烦。

尤其是有些内网项目，本身已有较好的安全保证。此时就可以另外一种方式，也就是 **白名单** 来处理
也就是 LimitType.WHITE_LIST

或在白名单之上追加限流规则，保障系统的可用性，也就是 LimitType.DEFAULT_WITH_WHITE_LIST


### 三、动态配置黑白名单
> 1.0.3 版本开始提供IpLimitUtils工具类，通过注入获取实例后可以实现动态配置黑白名单，该动态配置数据与注解中的配置取并集

***IpLimitUtils提供方法如下***
- putWhiteIpGroup - 可通过该方法动态配置新增白名单
- removeWhiteIpGroup - 可通过该方法动态清空对应 group 的白名单配置
- deleteWhiteIpGroupArrayStr - 可通过该方法动态去掉对应 group 中的某项 arrayStr 白名单
- putBlackIpGroup - 可通过该方法动态配置新增黑名单
- removeBlackIpGroup - 可通过该方法动态清空对应 group 的黑名单配置
- deleteBlackIpGroupArrayStr - 可通过该方法动态去掉对应 group 中的某项 arrayStr 黑名单

***有了这些方法，就可以通过第三方（比如数据库）存储黑白名单数据，然后动态初始化、修改黑名单配置***

## 异常记录
1. 暂时不支持Spring-6.x


## 更新日志
> 加粗表示重要版本更新，删除线表示废弃版本，不建议使用
- ~~1.0.1~~ 实现滑动窗口限流模式
- 1.0.2 调整规范，添加样例项目链接
- ___1.0.3___ 开放用户动态配置黑白名单





## Ip-Limit 计划实现功能
- 用户自定义限流器
- 全局限流、全局分IP限流
- 添加限流监控，监控数据回调（目前可以通过@ExceptionHandler(IpLimitException.class)处理异常回调）
- IP缓存统计数据可更换其他存储数据源，避免过多占用JVM缓存
- 可将IP更换为指定字段（比如账号）限流
- 更加灵活的异常处理机制
- 支持Spring-Gateway