package com.chuangjiangx.util;

import static com.chuangjiangx.util.StringUtils.replaceQuotes;
import static com.chuangjiangx.util.TypeUtils.initTypeArgs;
import static com.chuangjiangx.util.TypeUtils.isArray;
import static com.chuangjiangx.util.TypeUtils.isCommonType;
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
 * 形参工具类
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
                //TODO...简单类型形参（只判断基本类型，date和String） //TODO。。。rest URI
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
                fieldComment.setArg(initTypeArgs(parameter.type().simpleTypeName()));
                fieldComments.add(fieldComment);
            }
        }
        return fieldComments;
    }


    /**
     * 解析返回值
     * TODO 方法太复杂，待优化
     */
    public static FieldComment inspectReturn(MethodDoc methodDoc) {
        FieldComment fieldComment = new FieldComment();
        List<FieldComment> fieldComments = new ArrayList<>();
        if (methodDoc.returnType().simpleTypeName().equals("void")) {
            fieldComment.setTypeName("void");
        } else {
            if (typeValue(methodDoc.returnType().simpleTypeName()) == null) {
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
                    if (field.isFinal()) {
                        continue;
                    }
                    FieldComment comment = inspectReturnField(field);
                    if (comment.getName().equals("data")) {
                        inspectObjectTypeInResponse(methodDoc, comment);
                    }
                    fieldComments.add(comment);
                }
                returnComment(methodDoc, fieldComment);
            } else {
                //返回值为基本类型
                fieldComment.setTypeName(methodDoc.returnType().simpleTypeName());
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

    /**
     * 处理通用响应对象
     */
    private static FieldComment inspectReturnField(FieldDoc fieldDoc) {
        FieldComment comment = new FieldComment();
        return comment.inspectField(fieldDoc, 3);
    }

    /**
     * 处理Response中Object对象类型
     */
    private static void inspectObjectTypeInResponse(MethodDoc methodDoc,FieldComment comment) {
        Tag[] mapTags = methodDoc.tags("@map");
        SeeTag[] seeTags = methodDoc.seeTags();
        if (seeTags.length == 0) {
            log.error("响应对象中的Object对象类型未知,{}缺少@see类型注解", methodDoc.name());
            return;
//            throw new RuntimeException("响应对象中的Object对象类型未知,{}缺少@see类型注解");
        }
        if (mapTags.length > 0) {
            //map注解为1个时，表示data对象类型为list
            if (mapTags.length == 1) {
                comment.setTypeName("array");
                comment.setName(mapTags[0].text());
                ClassDoc dataDoc = seeTags[0].referencedClass();
                FieldDoc[] fieldDocs = dataDoc.fields(false);
                List<FieldComment> dataComments = new ArrayList<>();
                for (FieldDoc fieldDoc : fieldDocs) {
                    FieldComment dataComment = new FieldComment();
                    dataComment.inspectField(fieldDoc, 3);
                    dataComments.add(dataComment);
                }
                comment.setFieldComments(dataComments);
            } else {
                //多个map注解时，表示data对象类型为map
                comment.setTypeName("object");
                List<FieldComment> comments = new ArrayList<>();
                for (int i = 0; i < mapTags.length; i++) {
                    if (seeTags.length - 1 < i) {
                        continue;
                    }
                    FieldComment fcomment = new FieldComment();
                    String mapName = mapTags[i].text();
                    String[] split = mapName.split(" ");
                    fcomment.setName(split[0]);
                    fcomment.setTypeName("object");
                    if (split.length > 1) {
                        if (isArray(split[1])) {
                            fcomment.setTypeName("array");
                        }
                    }
                    List<FieldComment> fcomments = new ArrayList<>();
                    ClassDoc refClassDoc = seeTags[i].referencedClass();
                    if (isCommonType(refClassDoc.simpleTypeName())) {
                        fcomment.setTypeName(typeValue(refClassDoc.simpleTypeName()));
                        fcomment.setArg(initTypeArgs(refClassDoc.simpleTypeName()));
                        comments.add(fcomment);
                    } else {
                        FieldDoc[] fieldDocs = refClassDoc.fields(false);
                        for (FieldDoc fieldDoc : fieldDocs) {
                            FieldComment docComment = new FieldComment();
                            docComment.inspectField(fieldDoc, 3);
                            fcomments.add(docComment);
                        }
                        fcomment.setFieldComments(fcomments);
                        comments.add(fcomment);
                    }
                }
                comment.setFieldComments(comments);
            }
        } else {
            ClassDoc dataDoc = seeTags[0].referencedClass();
            if (isCommonType(dataDoc.simpleTypeName())) {
                comment.setTypeName(typeValue(dataDoc.simpleTypeName()));
                comment.setName("data");
                Tag[] tags = methodDoc.tags("@return");
                if (tags == null || tags[0].text() == null) {
                    comment.setArg(initTypeArgs(dataDoc.simpleTypeName()));
                } else {
                    comment.setArg(tags[0].text());
                }
            } else {
                comment.setTypeName("object");
                comment.setName("data");
                FieldDoc[] fieldDocs = dataDoc.fields(false);
                List<FieldComment> dataComments = new ArrayList<>();
                for (FieldDoc fieldDoc : fieldDocs) {
                    FieldComment dataComment = new FieldComment();
                    dataComment.inspectField(fieldDoc, 3);
                    dataComments.add(dataComment);
                }
                comment.setFieldComments(dataComments);
            }
        }
    }
}
