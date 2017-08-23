package com.chuangjiangx.util;

import java.util.Objects;

/**
 * @author Tzhou on 2017/8/20.
 */
public class StringUtils {

    /**
     * 去掉字符串中的引号
     * @param quotedString 字符串
     * @return
     */
    public static String replaceQuotes(String quotedString) {
        if (quotedString == null) {
            return null;
        }
        return quotedString.replaceAll("\"","");
    }

}
