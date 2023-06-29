package com.siiri.limiter.core.constant;


/**
 * 常量类
 * @author van
 */
public class IpLimitConstant {
    private IpLimitConstant() {}

    /**
     * 默认的通用组名
     */
    public static final String COMMON_LIMIT_GROUP = "COMMON_GROUP";

    /**
     * properties中配置Ip时的分隔符
     */
    public static final String IP_PROPERTIES_SPLIT = ",";

    /**
     * IP模糊匹配时的分隔符
     */
    public static final String IP_FUZZY_SPLIT = "*";

    /**
     * IPV4分几段
     */
    public static final Integer IPV4_SEGMENT_SIZE = 4;

    /**
     * 单*模糊匹配场景时,应当分成两端,应对规则设置为 172.*.1  这种场景
     */
    public static final Integer IPV4_SPLIT_TWO_SIZE = 2;

    /**
     * 单*模糊匹配场景时,应当分成两端,应对规则设置为 172.* / *.1 这种场景
     */
    public static final Integer IPV4_START_END_SPLIT = 1;
}
