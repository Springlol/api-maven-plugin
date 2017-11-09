package com.chuangjiangx.model;

import static com.chuangjiangx.util.DocUtils.findMethodAnnotationValue;
import static com.chuangjiangx.util.StringUtils.replaceQuotes;

import com.sun.javadoc.MethodDoc;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法注释信息
 *
 * @author Tzhou
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("unused")
public class MethodComment extends AbstractComment {
    /**
     * 请求uri
     */
    private String uri;
    /**
     * 请求uri是否为rest风格,默认为普通类型
     */
    private boolean isRest = false;
    /**
     * 请求映射
     */
    private String requestMapping;
    /**
     * http请求方法
     */
    private RequestMethod requestMethod;
    /**
     * 请求文本类型
     */
    private String reqContentType;
    /**
     * 响应文本类型
     */
    private String respContentType;
    /**
     * 参数注释
     */
    private List<FieldComment> methodArgumentComments = new ArrayList<>();
    /**
     * 返回值注释
     */
    private FieldComment methodReturnComment;

    /**
     * 解析方法
     */
    public void inspectMethod(MethodDoc methodDoc) {
        this.setComment(methodDoc.commentText() == null ? "(请添加注释信息)" : methodDoc.commentText() );
        this.setRawComment(methodDoc.getRawCommentText());
        //处理methodMapping
        String methodMapping = findMethodAnnotationValue(methodDoc, RequestMapping.class, "value");
        if (!replaceQuotes(methodMapping).startsWith("/")) {
            methodMapping = "/" + methodMapping;
        }
        this.requestMapping = replaceQuotes(methodMapping);
        //处理请求uri是否为rest风格
        if (methodMapping.contains("{") && methodMapping.contains("}")) {
            this.isRest = true;
        }
        //处理RequestMethod
        String requestMethod = findMethodAnnotationValue(methodDoc, RequestMapping.class, "method");
        if (requestMethod != null) {
            RequestMethod method = RequestMethod.getByName(requestMethod.substring(requestMethod.lastIndexOf(".") + 1));
            this.requestMethod = method == null ? RequestMethod.POST : method;
        } else { //默认POST
            this.requestMethod = RequestMethod.POST;
        }
    }

}
