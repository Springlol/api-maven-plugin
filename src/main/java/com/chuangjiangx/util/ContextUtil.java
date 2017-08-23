package com.chuangjiangx.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tzhou on 2017/8/19.
 */
public class ContextUtil {
    private static Map<String,String> CONTEXT_MAP = new HashMap<>();

    public static final String OUTPUT_KEY = "output";

    public static String get(Object key) {
        return CONTEXT_MAP.get(key);
    }

    public static String put(String key, String value) {
        return CONTEXT_MAP.put(key, value);
    }
}
