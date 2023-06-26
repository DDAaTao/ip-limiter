package com.siiri.limiter.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author van
 */
class IpUtilsTest {

    @Test
    void ipFuzzyMatchTest() {
        String matchStr1 = "172.*.*.*";
        String matchStr2 = "172.*";
        String matchStr3 = "*";
        String matchStr4 = "*.21";
        String matchStr5 = "172.16.50.21";

        String ip1 = "172.16.50.21";
        String ip2 = "173.16.50.21";
        String ip3 = "172.16.50.22";
        String ip4 = "172.17.51.21";

        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr1, ip1));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr2, ip1));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr3, ip1));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr4, ip1));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr5, ip1));

        Assertions.assertFalse(IpUtils.ipFuzzyMatch(matchStr1, ip2));

        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr2, ip1));
        Assertions.assertFalse(IpUtils.ipFuzzyMatch(matchStr2, ip2));

        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr3, ip1));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr3, ip2));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr3, ip3));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr3, ip4));

        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr4, ip1));
        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr4, ip2));
        Assertions.assertFalse(IpUtils.ipFuzzyMatch(matchStr4, ip3));

        Assertions.assertTrue(IpUtils.ipFuzzyMatch(matchStr5, ip1));
        Assertions.assertFalse(IpUtils.ipFuzzyMatch(matchStr5, ip2));
        Assertions.assertFalse(IpUtils.ipFuzzyMatch(matchStr5, ip3));
        Assertions.assertFalse(IpUtils.ipFuzzyMatch(matchStr5, ip4));
    }
}
