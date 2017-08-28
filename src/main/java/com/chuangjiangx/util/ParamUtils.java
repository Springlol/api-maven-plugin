package com.chuangjiangx.util;

import static com.chuangjiangx.util.StringUtils.replaceQuotes;
import static com.chuangjiangx.util.TypeUtils.initTypeArgs;
import static com.chuangjiangx.util.TypeUtils.typeValue;

import com.chuangjiangx.model.ContentType;
import com.chuangjiangx.model.FieldComment;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tzhou on 2017/8/22.
 */
@Slf4j
public class ParamUtils {

    /**
     * 解析请求文本类型
     */
    public static String detectContentType(MethodDoc methodDoc) {
        Parameter[] parameters = methodDoc.parameters();
        for (Parameter parameter : parameters) {
            if (parameter.type().simpleTypeName().equals("MultipartFile")) {
                return ContentType.FORM_DATA.content;
            }
            if (DocUtils.isHaveAnno(parameter, RequestBody.class)) {
                return ContentType.RAW_JSON.content;
            }
        }
        //默认类型
        return ContentType.X_WWW_FORM_URLENCODED.content;
    }


    /**
     * 解析形参
     */
    public static List<FieldComment> inspectParam(RootDoc rootDoc, MethodDoc methodDoc) {
        List<FieldComment> fieldComments = new ArrayList<>();
        for (Parameter parameter : methodDoc.parameters()) {
            //忽略Session等形参
            if ("HttpSession".equals(parameter.typeName())
                    || "HttpServletResponse".equals(parameter.typeName())
                    || "HttpServletRequest".equals(parameter.typeName())
                    || "BindingResult".equals(parameter.typeName())) {
                continue;
            }
            String typeValue = typeValue(parameter.type().simpleTypeName());
            if (typeValue == null) {
                //对象形参
                String qualifiedTypeName = parameter.type().qualifiedTypeName();
                ClassDoc classDoc = rootDoc.classNamed(qualifiedTypeName);
                if (classDoc == null) {
                    log.error("未找到{}的源文件", qualifiedTypeName);
                    continue;
                }
                FieldDoc[] fields = classDoc.fields(false);
                ArrayList<FieldDoc> fieldDocs = new ArrayList<>();
                fieldDocs.addAll(Arrays.asList(fields));
                ClassDoc superclass = classDoc.superclass();
                if (superclass != null) {
                    //处理父类中字段
                    fieldDocs.addAll(Arrays.asList(superclass.fields(false)));
                }
                for (FieldDoc field : fieldDocs) {
                    FieldComment paramComment = new FieldComment();
                    paramComment = paramComment.inspectField(field, 3);
                    fieldComments.add(paramComment);
                }
            } else {
                //TODO...简单类型形参（只判断基本类型，date和String）
                FieldComment fieldComment = new FieldComment();
                fieldComment.setTypeName(typeValue(parameter.type().simpleTypeName()));
                fieldComment.setRequired(true);
                ParamTag[] paramTags = methodDoc.paramTags();
                for (ParamTag paramTag : paramTags) {
                    if (paramTag.parameterName().equals(parameter.name())) {
                        fieldComment.setComment(paramTag.parameterComment());
                    }
                }
                String value = DocUtils.findParamAnnotionValue(parameter, RequestParam.class, "value");
                fieldComment.setName(value == null ? parameter.name() : replaceQuotes(value));
                fieldComment.setArg(initTypeArgs(parameter.type()));
                fieldComments.add(fieldComment);
            }
        }
        return fieldComments;
    }


    /**
     * 解析返回值
     */
    public static FieldComment inspectReturn(MethodDoc methodDoc) {
        FieldComment fieldComment = new FieldComment();
        List<FieldComment> fieldComments = new ArrayList<>();
        if (methodDoc.returnType().simpleTypeName().equals("void")) {
            fieldComment.setTypeName("void");
        } else {
            if (typeValue(methodDoc.returnType().simpleTypeName()) == null) {
                Tag[] arrTags = methodDoc.tags("@map");
                SeeTag[] seeTags = methodDoc.seeTags();
                ClassDoc classDoc = methodDoc.returnType().asClassDoc();
                FieldDoc[] fields = classDoc.fields(false);
                ClassDoc superclass = classDoc.superclass();
                List<FieldDoc> fieldDocList = new ArrayList<>();
                fieldDocList.addAll(Arrays.asList(fields));
                if (superclass != null) {
                    FieldDoc[] superFields = superclass.fields(false);
                    fieldDocList.addAll(Arrays.asList(superFields));
                }
                for (FieldDoc field : fieldDocList) {
                    FieldComment comment = new FieldComment();
                    if (field.type().simpleTypeName().equals("Object")) {
                        //操作Response中的data字段
                        if (seeTags.length == 0) {
                            log.error("响应对象中的Object对象类型未知,{}缺少@see类型注解", methodDoc.name());
                            continue;
                        }
                        //TODO...只处理第一个@map，@see注解
                        ClassDoc dataDoc = seeTags[0].referencedClass();
                        FieldDoc[] fieldDocs = dataDoc.fields(false);
                        comment.setComment(field.commentText());
                        if (arrTags.length > 0) {
                            comment.setTypeName("array");
                            comment.setName(arrTags[0].text());
                        } else {
                            comment.setTypeName("object");
                            comment.setName(field.name());
                        }
                        List<FieldComment> dataComments = new ArrayList<>();
                        for (FieldDoc fieldDoc : fieldDocs) {
                            FieldComment dataComment = new FieldComment();
                            dataComment.inspectField(fieldDoc, 3);
                            dataComments.add(dataComment);
                        }
                        comment.setFieldComments(dataComments);
                    } else {
                        comment.inspectField(field, 3);
                    }
                    fieldComments.add(comment);
                }
                returnComment(methodDoc, fieldComment);
            } else {
                //返回值为基本类型
                log.info("{}接口返回值为基本类型,未做处理", methodDoc.name());
            }
        }
        fieldComment.setFieldComments(fieldComments);
        return fieldComment;
    }

    private static void returnComment(MethodDoc methodDoc, FieldComment fieldComment) {
        Tag[] tags = methodDoc.tags("@return");
        if (tags.length == 0) {
            log.error("方法{}缺少@return注释", methodDoc.name());
        } else {
            String text = tags[0].text();
            fieldComment.setComment(text);
            fieldComment.setTypeName(typeValue(methodDoc.returnType().simpleTypeName()));
        }
    }


}
