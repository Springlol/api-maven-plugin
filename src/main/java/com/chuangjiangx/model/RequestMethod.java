package com.chuangjiangx.model;

/**
 * @author by zhoutao on 2017/11/7.
 */
@SuppressWarnings("unused")
public enum RequestMethod {
    GET,
    POST,
    HEAD,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE;

    public static RequestMethod getByName(String method) {
        for (RequestMethod requestMethod : RequestMethod.values()) {
           if (requestMethod.name().equals(method)) {
               return requestMethod;
           }
        }
        return null;
    }
}
