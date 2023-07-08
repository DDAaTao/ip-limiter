package com.van.limiter.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.van.limiter.core.annotation.IpLimit;
import com.van.limiter.core.constant.IpLimitConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 通过注入获取实例后可以实现动态配置黑白名单
 * @author van
 */
@Component
public class IpLimitUtils {

    /**
     * 动态配置白名单Map，[Group,[IpStr]]
     */
    private static final Map<String, List<String>> WHITE_IP_LIST = Maps.newHashMap();

    /**
     * 动态配置黑名单Map，[Group,[IpStr]]
     */
    private static final Map<String, List<String>> BLACK_IP_LIST = Maps.newHashMap();

    @Autowired
    private Environment environment;


    /**
     * 可通过该方法动态配置新增白名单
     * @param groupName 要配置的groupName
     * @param arrayStr 对应的IPStr，支持多种格式
     * @return success
     */
    public boolean putWhiteIpGroup(String groupName, String arrayStr) {
        List<String> arrayStrList = WHITE_IP_LIST.computeIfAbsent(groupName, k -> Lists.newArrayList());
        return arrayStrList.add(arrayStr);
    }

    /**
     * 可通过该方法动态清空对应 group 的白名单配置
     * @param groupName groupName
     * @return 被清空的白名单数据
     */
    public List<String> removeWhiteIpGroup(String groupName) {
        return WHITE_IP_LIST.remove(groupName);
    }

    /**
     * 可通过该方法动态去掉对应 group 中的某项 arrayStr 白名单
     * @param groupName groupName
     * @param arrayStr arrayStr
     * @return 去掉后的 group 白名单情况
     */
    public List<String> deleteWhiteIpGroupArrayStr(String groupName, String arrayStr) {
        return WHITE_IP_LIST.computeIfPresent(groupName, (k, v) -> {
            v.remove(arrayStr);
            return v;
        });
    }

    /**
     * 可通过该方法动态配置新增黑名单
     * @param groupName 要配置的groupName
     * @param arrayStr 对应的IPStr，支持多种格式
     * @return success
     */
    public boolean putBlackIpGroup(String groupName, String arrayStr) {
        List<String> arrayStrList = BLACK_IP_LIST.computeIfAbsent(groupName, k -> Lists.newArrayList());
        return arrayStrList.add(arrayStr);
    }


    /**
     * 可通过该方法动态清空对应 group 的黑名单配置
     * @param groupName groupName
     * @return 被清空的黑名单数据
     */
    public List<String> removeBlackIpGroup(String groupName) {
        return BLACK_IP_LIST.remove(groupName);
    }

    /**
     * 可通过该方法动态去掉对应 group 中的某项 arrayStr 黑名单
     * @param groupName groupName
     * @param arrayStr arrayStr
     * @return 去掉后的 group 黑名单情况
     */
    public List<String> deleteBlackIpGroupArrayStr(String groupName, String arrayStr) {
        return BLACK_IP_LIST.computeIfPresent(groupName, (k, v) -> {
            v.remove(arrayStr);
            return v;
        });
    }

    /**
     * 判断IP是否在白名单列表里
     * @param ipLimit ipLimit
     * @param ip ip
     * @return 是否存在
     */
    public boolean ipInWhiteIpList(IpLimit ipLimit, String ip) {
        boolean annData = strInIpArray(ipLimit.whiteList(), ip);
        List<String> configWhites = WHITE_IP_LIST.get(ipLimit.groupName());
        if (configWhites != null && !configWhites.isEmpty()) {
            // 动态配置Map或注释中只要存在一个就算命中
            return annData || strInIpList(configWhites, ip);
        }
        return annData;
    }

    /**
     * 判断IP是否在黑名单列表里
     * @param ipLimit ipLimit
     * @param ip ip
     * @return 是否存在
     */
    public boolean ipInBlackIpList(IpLimit ipLimit, String ip) {
        boolean annData = strInIpArray(ipLimit.blackList(), ip);
        List<String> configBlacks = BLACK_IP_LIST.get(ipLimit.groupName());
        if (configBlacks != null && !configBlacks.isEmpty()) {
            // 动态配置Map或注释中只要存在一个就算命中
            return annData || strInIpList(configBlacks, ip);
        }
        return annData;
    }

    /**
     * 判断对应字符串是否存在于数组内
     * @param array array
     * @param str 字符串
     * @return boolean
     */
    public boolean strInIpArray(String[] array, String str) {
        for (String arrayStr : array) {
            if (ipMatch(arrayStr, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断对应字符串是否存在于数组内
     * @param array array
     * @param str 字符串
     * @return boolean
     */
    public boolean strInIpList(List<String> array, String str) {
        for (String arrayStr : array) {
            if (ipMatch(arrayStr, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过environment实现可通过properties配置对应的映射arrayStr
     * @param arrayStr 组合Str
     * @param ip ip
     * @return 是否包含
     */
    private boolean ipMatch(String arrayStr, String ip) {
        arrayStr = environment.resolvePlaceholders(arrayStr);
        String[] split = arrayStr.split(IpLimitConstant.IP_PROPERTIES_SPLIT);
        for (String s : split) {
            if (IpUtils.ipFuzzyMatch(s.trim(), ip)) {
                return true;
            }
        }
        return false;
    }
}
