package com.chuangjiangx.model;

import static com.chuangjiangx.util.DocUtils.isHaveNotNull;
import static com.chuangjiangx.util.StringUtils.replaceQuotes;
import static com.chuangjiangx.util.TypeUtils.initTypeArgs;
import static com.chuangjiangx.util.TypeUtils.isArray;
import static com.chuangjiangx.util.TypeUtils.isCommonType;
import static com.chuangjiangx.util.TypeUtils.typeValue;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段注释信息
 *
 * @author Tzhou on 2017/8/19.
 */
@Getter
@Setter
@Slf4j
public class FieldComment extends AbstractComment {
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段类型
     */
    private String typeName;
    /**
     * 是否必须,默认为false
     */
    private boolean required = false;
    /**
     * 当前字段类型为对象类型
     */
    private List<FieldComment> fieldComments = new ArrayList<>();
    /**
     * 字段例子值
     */
    private String arg;


    /**
     * 解析字段(无法处理泛型字段 例如 T t)
     */
    public FieldComment inspectField(FieldDoc fieldDoc, int times) {
        if (isCommonType(fieldDoc.type().simpleTypeName())) {
            //基本类型
            inspectCommonType(fieldDoc);
        } else {
            inspectObjectType(fieldDoc, times);
        }
        return this;
    }

    /**
     * 解析基本类型
     */
    private void inspectCommonType(FieldDoc fieldDoc) {
        this.setComment(fieldDoc.commentText());
        this.setRawComment(fieldDoc.getRawCommentText());
        this.name = fieldDoc.name();
        this.typeName = replaceQuotes(typeValue(fieldDoc.type().simpleTypeName()));

        Tag[] tags = fieldDoc.tags("@arg");
        if (tags.length > 0) {
            this.arg = tags[0].text();
        } else {
            this.arg = initTypeArgs(fieldDoc.type());
        }

        if (isHaveNotNull(fieldDoc)) {
            this.required = true;
        }
    }

    /**
     * 解析对象类型
     */
    private void inspectObjectType(FieldDoc fieldDoc, int times) {
        --times;
        inspectCommonType(fieldDoc);
        if (isArray(fieldDoc.type())) {
            //集合必须指定泛型类型
            this.typeName = "array";
            ParameterizedType parameterizedType = fieldDoc.type().asParameterizedType();
            if (parameterizedType == null) {
                log.error("请指定{}集合字段的泛型类型", fieldDoc.name());
            } else {
                Type[] types = parameterizedType.typeArguments();
                if (! isCommonType(types[0].simpleTypeName())) {
                    //如果集合泛型类型为对象类型
                    ClassDoc classDoc = types[0].asClassDoc();
                    if (classDoc == null) {
                        log.error("未找到{}类型的源文件", types[0].qualifiedTypeName());
                    } else {
                        inspectModel(classDoc, times);
                    }
                } else {
                    //泛型为基本类型
                    this.arg = initTypeArgs(types[0]);
                }
            }
        } else {
            this.typeName = "object";
            ClassDoc classDoc = fieldDoc.type().asClassDoc();
            inspectModel(classDoc, times);
        }
    }

    /**
     * 解析字段嵌套对象
     */
    private void inspectModel(ClassDoc classDoc, int times) {
        if (times <= 0) {
            return;
        }
        FieldDoc[] fields = classDoc.fields(false);
        List<FieldComment> fieldComments = new ArrayList<>();
        for (FieldDoc field : fields) {
            FieldComment fieldComment = new FieldComment();
            fieldComments.add(fieldComment.inspectField(field, times));
        }
        this.fieldComments = fieldComments;
    }
}
