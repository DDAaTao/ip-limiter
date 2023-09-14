~~~
author: van , ggfanwentao@gmail.com
~~~
中文 | [点击跳转](README_zh.md)

---

# Ip-Limiter: Lightweight Annotation-based IP Rate Limiting Component

## Project Introduction
Ip-Limiter is a lightweight, annotation-based IP rate limiting component based on JVM cache, designed for easy integration into projects and to meet multi-threading scenarios.

### Usage Example
> Contains detailed demonstration code

Demo Project Repository: https://github.com/DDAaTao/ip-limiter-example \
Demo China Project Repository: https://gitee.com/fanwentaomayun/ip-limiter-example

**Ip-Limit features include:**
- Annotation-based usage, simple and convenient, can be added to Controller classes or specific API methods.
- Minimal intrusion into business logic; no need for extensive configuration, yet supports various scenarios.
- Provides group-level uniform rate limiting, serving both single interface and multiple interfaces within a group.
- Supports configuration through property files and dynamic external configuration (addition and deletion) of blacklists and whitelists.

```properties
# Configuration in a properties file, note that ',' is used as a delimiter
my.white.ip.list=172.16.50.21,172.16.50.22,172.16.50.23
```

```
// Usage in code
@IpLimit(limitType = LimitType.WHITE_LIST, whiteList = "${my.white.ip.list}")
// or
@IpLimit(limitType = LimitType.WHITE_LIST, whiteList = {"${my.white.ip.list}","172.16.50.35"})
```

- Black and white list IP rules support multiple fuzzy pattern configurations and IPv6:
  - 172.\*.\*.1
  - 172.*.1
  - 172.*
  - *.21
  - \*

**Core rate limiting modes - LimitType class:**
- DEFAULT - Follows the default rate limiting strategy, without considering black and white list parameters.
- WHITE_LIST - Considers only the whitelist strategy; requests not in the whitelist are all rejected.
- BLACK_LIST - Considers only the blacklist strategy; requests not in the blacklist are not rate-limited.
- DEFAULT_WITH_WHITE_LIST - Builds upon the default rate limiting strategy, where IPs in the whitelist are not rate-limited.
- DEFAULT_WITH_BLACK_LIST - Builds upon the default rate limiting strategy, directly returning a 403 error for IPs in the blacklist.
- DEFAULT_WITH_WHITE_AND_BLACK_LIST - Builds upon the default rate limiting strategy, directly returning a 403 error for IPs in the blacklist and allowing IPs in the whitelist to proceed.

## Getting Started

1. Include the Ip-Limit dependency (available on Maven Central).
```xml
  <!-- Recommend using the latest version {ip-limiter.version} -->
  <dependency>
    <groupId>io.github.DDAaTao</groupId>
    <artifactId>ip-limiter</artifactId>
    <version>1.0.3</version>
  </dependency>
```
2. Add @EnableIpLimit to the web application class or any class that can be scanned by Spring.
3. Add the @IpLimit annotation to the methods (endpoints) where you want to apply IP rate limiting, and adjust the parameters according to your needs.

> If your project does not include guava and spring-context packages, you need to manually include them; otherwise, you may encounter a java.lang.NoSuchMethodError exception.
>
> Starting from version 1.0.1, these dependencies are included by default. If your project already has these dependencies, consider excluding them.

## Best Practices
### 1.Custom Rate Limit Exception Handling Mechanism
```Java
/**
 * By default, when requests exceed rate limits, it logs an error and throws an IpLimitException.
 * Users can capture and customize the exception handling through a global exception handler.
 * Callbacks or hook methods may be added in the future.
 * */
@Slf4j
@ControllerAdvice
public class BaseExceptionHandler {

  @ExceptionHandler(IpLimitException.class)
  @ResponseBody
  public RestApiResult<Object> resolveCommonException(IpLimitException e) {
    log.error("IpLimitException Intercept. Please try again later.. " + e.getMessage());
    // Here, you can perform rate limit callback processing using e.getRequestIp() and e.getGroupName()
    return RestApiResult.fail("IpLimitException Intercept. Please try again later.. ");
  }
  
}
```

### 2. Integration with Existing Authentication Solutions

In SpringCloud projects or most projects, there is usually an existing authentication mechanism, such as Spring Security. In such cases, when there is a need to integrate with external interfaces, there are two approaches. One is to handle it through third-party protocols like OAuth2, but this can be a complex integration process.

Especially in the case of internal network projects that already have robust security measures, another approach can be used, which is the whitelist method, denoted as LimitType.WHITE_LIST.

Alternatively, you can add rate limiting rules on top of the whitelist to ensure system availability, using LimitType.DEFAULT_WITH_WHITE_LIST.

### 3. Dynamic Configuration of Black and White Lists
> Starting from version 1.0.3, IpLimitUtils utility class is provided, which allows dynamic configuration of black and white lists. This dynamic configuration data is combined with the configuration specified in annotations.

***IpLimitUtils offers the following methods***
- `putWhiteIpGroup` - Allows dynamic addition of new entries to the whitelist.
- `removeWhiteIpGroup` - Dynamically clears the whitelist for a specific group.
- `deleteWhiteIpGroupArrayStr` - Allows dynamic removal of a specific arrayStr entry from the whitelist for a group.
- `putBlackIpGroup` - Allows dynamic addition of new entries to the blacklist.
- `removeBlackIpGroup` - Dynamically clears the blacklist for a specific group.
- `deleteBlackIpGroupArrayStr` - Allows dynamic removal of a specific arrayStr entry from the blacklist for a group.

***With these methods, you can store black and white list data in third-party sources like databases and then dynamically initialize or modify blacklist configurations.***

## Known Issues
1. Currently, it does not support Spring 6.x.

## Changelog
> Bold indicates important version updates; strikethrough indicates deprecated versions, not recommended for use.

- ~~1.0.1~~ Implemented sliding window rate limiting mode.
- 1.0.2 Adjusted specifications and added a link to example projects.
- ___1.0.3___ Introduced dynamic configuration of black and white lists for user customization.


## Ip-Limit Planned Features
- User-customized rate limiters.
- Global rate limiting and per-IP rate limiting.
- Adding rate limiting monitoring and callback for monitoring data (currently handled via @ExceptionHandler(IpLimitException.class)).
- IP cache statistics data can be stored in other data sources to avoid excessive JVM cache usage.
- Support for using a specified field (e.g., account) for rate limiting.
- More flexible exception handling mechanisms.
- Support Spring-Gateway



