package com.chuangjiangx.model;

import static com.chuangjiangx.util.DocUtils.findClassAnnotationValue;
import static com.chuangjiangx.util.StringUtils.replaceQuotes;

import com.sun.javadoc.ClassDoc;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 类注释信息
 *
 * @author Tzhou on 2017/8/19.
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("unused")
public class ClassComment extends AbstractComment {
    /**
     * 类名
     */
    private String className;

    private String requestMapping;

    private String produces;

    /**
     * 方法信息
     */
    private List<MethodComment> methodComments = new ArrayList<>();

    /**
     * 字段信息
     */
    private List<FieldComment> fieldComments = new ArrayList<>();

    /**
     * 解析类信息
     */
    public void inspectClass(ClassDoc classDoc) {
        //类注释
        this.setComment(classDoc.commentText());
        this.setRawComment(classDoc.getRawCommentText());
        this.className = classDoc.name();
        this.requestMapping = replaceQuotes(findClassAnnotationValue(classDoc, RequestMapping.class, "value"));
        this.produces = replaceQuotes(findClassAnnotationValue(classDoc, RequestMapping.class, "produces"));
    }

}
