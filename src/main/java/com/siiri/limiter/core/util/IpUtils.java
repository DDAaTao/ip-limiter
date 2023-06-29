package com.siiri.limiter.core.util;

import com.google.common.collect.Sets;
import com.siiri.limiter.core.constant.IpLimitConstant;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * IP 工具类
 * @author van
 */
public class IpUtils {
    private IpUtils() {}

    /**
     * 内网ip正则
     * <ul>
     *     <li>127.0.0.1 或 localhost</li>
     *     <li>10.x.x.x</li>
     *     <li>172.16.x.x - 172.31.x.x</li>
     *     <li>192.168.x.x</li>
     * </ul>
     */
    private static final Pattern LAN_PATTERN = Pattern.compile("^(127\\.0\\.0\\.1)|(localhost)" +
            "|(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})" +
            "|(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})" +
            "|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");
    /**
     * localhost set
     */
    private static final Set<String> LOCAL_HOST_SET = Sets.newHashSet("0:0:0:0:0:0:0:1", "localhost");

    /**
     * 判断ip是否为空，空返回true
     *
     * @param ip ip
     * @return bol
     */
    public static boolean isEmptyIp(final String ip) {
        return (ip == null || ip.length() == 0 || ip.trim().equals("") || "unknown".equalsIgnoreCase(ip));
    }


    /**
     * 判断ip是否不为空，不为空返回true
     *
     * @param ip ip
     * @return bol
     */
    public static boolean isNotEmptyIp(final String ip) {
        return !isEmptyIp(ip);
    }

    /***
     * 获取客户端ip地址(可以穿透代理)
     * @param request HttpServletRequest
     * @return ip
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取本机的局域网ip地址，兼容Linux
     *
     * @return String
     */
    public String getLocalHostIP() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        String localHostAddress = "";
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = allNetInterfaces.nextElement();
            Enumeration<InetAddress> address = networkInterface.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress inetAddress = address.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    localHostAddress = inetAddress.getHostAddress();
                }
            }
        }
        return localHostAddress;
    }

    /**
     * 判断是否为局域网ip
     * @param ip ip
     * @return boolean
     */
    private static boolean isLAN(String ip) {
        ip = ip.trim();
        return LAN_PATTERN.matcher(ip).matches() || LOCAL_HOST_SET.contains(ip.toLowerCase());
    }

    /**
     * 是否是内网访问
     * @param request HttpServletRequest
     * @return boolean
     */
    public static boolean isLanAccess(HttpServletRequest request) {
        String host = getRequestHost(request);
        return isLAN(host);
    }

    public static String getRequestHost(HttpServletRequest request) {
        String remoteHost = request.getHeader("host");
        if (LOCAL_HOST_SET.contains(remoteHost)) {
            return "localhost";
        }
        int colonIndex = remoteHost.indexOf(":");
        return colonIndex > 0 ? remoteHost.substring(0, colonIndex) : remoteHost;
    }

    /**
     * 判断 ip 是否满足模糊匹配 matchIpPattern
     * @param matchIpPattern 模糊匹配规则,支持多种模式 eg. 172.*.21 ; 172.*.*.21;
     * @param ip 需要进行匹配的ip
     * @return isAccess
     */
    public static boolean ipFuzzyMatch(String matchIpPattern, String ip) {
        if (!matchIpPattern.contains(IpLimitConstant.IP_FUZZY_SPLIT)) {
            return matchIpPattern.equals(ip);
        }

        if (matchIpPattern.equals(IpLimitConstant.IP_FUZZY_SPLIT)) {
            return true;
        }

        // 针对简易单星号场景
        String[] split = matchIpPattern.split("\\*");
        if (split.length == IpLimitConstant.IPV4_START_END_SPLIT) {
            return matchIpPattern.startsWith(IpLimitConstant.IP_FUZZY_SPLIT)
                    ? ip.endsWith(split[0])
                    : ip.startsWith(split[0]);
        }
        if (split.length == IpLimitConstant.IPV4_SPLIT_TWO_SIZE) {
            return ip.startsWith(split[0]) && ip.endsWith(split[1]);
        }

        // 不是一个星号的时候
        String[] matchStrSplit = matchIpPattern.split("\\.");
        String[] ipSplit = ip.split("\\.");
        for (int index = 0; index < IpLimitConstant.IPV4_SEGMENT_SIZE; index ++) {
            if (IpLimitConstant.IP_FUZZY_SPLIT.equals(matchStrSplit[index])) {
                continue;
            }
            if (!matchStrSplit[index].equals(ipSplit[index])) {
                return false;
            }
        }
        return true;
    }
}
