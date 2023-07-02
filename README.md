~~~ 
author: van , ggfanwentao@gmail.com
~~~
---

# Ip-Limit: 轻量级注解式IP限流组件

## 项目简介
基于JVM缓存的轻量级、注解式IP限流组件，方便项目快速引用，满足多线程场景。
初代版本考虑可以快速在项目应用，故先采用Guava的 RateLimiter 令牌桶模式作为限流器，
后续考虑实现滑动窗口模式

**Ip-Limit 具有以下特性:**
- 基于注解使用，简单快捷
- 业务入侵小，不用过多配置类 
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


## 快速开始

1. 引入Ip-Limit依赖（已发布至Maven中央仓库）
```xml
<dependency>
  <groupId>io.github.DDAaTao</groupId>
  <artifactId>ip-limiter</artifactId>
  <version>1.0.0</version>
</dependency>
```
2. 将 @EnableIpLimit 添加到 webApplication 类上,或其他可以被 Spring 扫描到的类上
3. 将 @IpLimit 注解添加到想要做IP限流的方法（接口）上，根据需求动态调整参数
