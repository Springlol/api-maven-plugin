package com.chuangjiangx.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tzhou on 2017/8/19.
 */
@SuppressWarnings("all")
public class ContextUtil {
    private static Map<String,Object> CONTEXT_MAP = new HashMap<>();

    public static final String OUTPUT_KEY = "output";

    public static final String FILTER_METHODS_KEY = "methods";

    public static final String MDTYPE_KEY = "mdType";

    public static Object get(Object key) {
        return CONTEXT_MAP.get(key);
    }

    public static Object put(String key, Object value) {
        return CONTEXT_MAP.put(key, value);
    }
}
