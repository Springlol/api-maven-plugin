package com.chuangjiangx.model;

/**
 * @author Tzhou on 2017/8/27.
 */
@SuppressWarnings("all")
public enum ContentType {
    FORM_DATA("multipart/form-data"),
    X_WWW_FORM_URLENCODED("application/x-www-from-urlencoded"),
    RAW_JSON("application/json"),
    RAW_XML("application/xml");
    public final String content;
    ContentType(String content) {
        this.content = content;
    }
}
