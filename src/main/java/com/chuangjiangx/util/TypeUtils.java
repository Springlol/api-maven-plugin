package com.chuangjiangx.util;

import com.sun.javadoc.Type;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基本类型工具
 * @author Tzhou on 2017/8/19.
 */
public class TypeUtils {
    //基本数据类型，对应md类型
    private static Map<String, String> commomTypes = new HashMap<String, String>() {
        {
            put("byte", "number");
            put("Byte", "number");
            put("boolean", "boolean");
            put("Boolean", "boolean");
            put("char", "string");
            put("Char", "string");
            put("short", "number");
            put("Short", "number");
            put("int", "number");
            put("Integer", "number");
            put("long", "number");
            put("Long", "number");
            put("float", "number");
            put("Float", "number");
            put("double", "number");
            put("Double", "number");
            put("BigDecimal", "number");
            put("BigInteger", "number");
            put("Number", "number");
            put("String", "string");
            put("Date", "string");
            put("HttpServletResponse", "object");
            put("MultipartFile", "string");
            put("BindingResult","object");
        }
    };
    //对应md中数组类型
    private static String[] arrays = new String[]{
            "List", "ArrayList", "LinkedList", "Set", "HashSet", "TreeSet", "SortedSet", "Collection"
    };

    @SuppressWarnings("unused")
    public static boolean isCommonType(String typeName) {
        return commomTypes.keySet().contains(typeName);
    }

    /**
     * 根据形参类型名获取对应md中类型名
     *
     * @param typeName 类型名称
     * @return md类型名称
     */
    public static String typeValue(String typeName) {
        Objects.requireNonNull(typeName);
        return commomTypes.get(typeName);
    }

    /**
     * 是否是集合或者数组
     *
     * @param type 类型
     * @return boolean
     */
    public static boolean isArray(Type type) {
        List<String> list = Arrays.asList(arrays);
        return list.contains(type.simpleTypeName());
    }

    /**
     * 初始化参数类型值
     */
    public static String initTypeArgs(Type type) {
        String typeValue = typeValue(type.simpleTypeName());
        if ("Date".equals(type.simpleTypeName())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(new Date());
        } else if ("string".equals(typeValue)) {
            return "default";
        } else if ("number".equals(typeValue)) {
            return "1";
        } else if ("boolean".equals(typeValue)) {
            return "true";
        } else {
            return null;
        }
    }
}
