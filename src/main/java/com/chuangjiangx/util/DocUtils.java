package com.chuangjiangx.util;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * @author Tzhou on 2017/8/19.
 */
public class DocUtils {

    public static List<ClassDoc> findValidClass(RootDoc rootDoc, String clazzName) {
        List<ClassDoc> classDocList = new ArrayList<>();
        for (ClassDoc classDoc : rootDoc.classes()) {
            if (classDoc.qualifiedTypeName().equals(clazzName)) {
                classDocList.add(classDoc);
            }
        }
        return classDocList;
    }

    public static List<ClassDoc> findValidClass(RootDoc rootDoc, Class... annotationClazzs) {
        List<ClassDoc> classDocList = new ArrayList<>();
        for (ClassDoc classDoc : rootDoc.classes()) {
            for (AnnotationDesc annotationDesc : classDoc.annotations()) {
                for (Class annotationClazz : annotationClazzs) {
                    if (annotationDesc.annotationType().qualifiedTypeName().equals(annotationClazz.getName())) {
                        classDocList.add(classDoc);
                    }
                }
            }
        }
        return classDocList;
    }

    public static List<MethodDoc> findValidMethod(ClassDoc classDoc, Class... annotationClazzs) {
        List<MethodDoc> methodDocList = new ArrayList<>();
        for (MethodDoc methodDoc : classDoc.methods(false)) {
            for (AnnotationDesc methodAnnotationDesc : methodDoc.annotations()) {
                for (Class annotationClazz : annotationClazzs) {
                    if (methodAnnotationDesc.annotationType().qualifiedTypeName().equals(annotationClazz.getTypeName())) {
                        methodDocList.add(methodDoc);
                    }
                }
            }
        }
        return methodDocList;
    }

    /*public static List<FieldComment> convertToFieldCommentList(RootDoc rootDoc, Parameter parameter) {
        List<FieldComment> fieldCommentList = new ArrayList<>();
        List<ClassDoc> parameterClassDocList = findValidClass(rootDoc, parameter.type().qualifiedTypeName());
        for (ClassDoc parameterClassDoc : parameterClassDocList) {
            List<FieldComment> classFieldCommentList = convertToFieldComment(rootDoc, parameterClassDoc, 5, 0);
            fieldCommentList.addAll(classFieldCommentList);
        }
        return fieldCommentList;
    }*/

    /**
     * 递归获取所有属性，超过递归次数之后停止继续获取
     *
     * @param times 当前次数
     * @param limit 递归次数限制
     */
   /* public static List<FieldComment> convertToFieldComment(RootDoc rootDoc, Type returnType, int limit, int times) {
        if (times > limit) {
            return null;
        }

        List<FieldComment> fieldCommentList = new ArrayList<>();
        Map<String, Type> parameterizedTypeMap = new HashMap<>();

        if (returnType.asParameterizedType() != null) {
            Type[] typeArguments = returnType.asParameterizedType().typeArguments();
            TypeVariable<? extends Class<?>>[] typeParameters = ReflectionUtils.forName(returnType.qualifiedTypeName()).getTypeParameters();
            for (int i = 0; i < typeArguments.length; i++) {
                parameterizedTypeMap.put(typeParameters[i].getName(), typeArguments[i]);
            }
        }

        for (FieldDoc fieldDoc : returnType.asClassDoc().fields(false)) {
            Type fieldType = parameterizedTypeMap.get(fieldDoc.type().qualifiedTypeName());
            if(fieldType == null){
                fieldType = fieldDoc.type();
            }
            List<ClassDoc> classDocList = findValidClass(rootDoc, fieldType.qualifiedTypeName());

            FieldComment fieldComment = new FieldComment();
            fieldComment.setComment(fieldDoc.commentText());
            fieldComment.setRawComment(fieldDoc.getRawCommentText());
            fieldComment.setName(fieldDoc.name());

            Type parameterizedType = parameterizedTypeMap.get(fieldDoc.type().qualifiedTypeName());
            String typeName;
            if (parameterizedType == null) {
                typeName = fieldDoc.type().qualifiedTypeName();
            } else {
                typeName = parameterizedType.qualifiedTypeName();
                classDocList.add(parameterizedType.asClassDoc());
            }
            fieldComment.setTypeName(typeName);

            for (ClassDoc doc : classDocList) {
                List<FieldComment> subFieldCommentList = convertToFieldComment(rootDoc, doc, limit, ++times);
                fieldComment.setFieldComments(subFieldCommentList);
            }
            fieldCommentList.add(fieldComment);
        }
        return fieldCommentList;
    }*/

    /**
     * 获取方法指定注解字段值
     * @param methodDoc 方法
     * @param annotationClass 指定注解
     * @param name 字段
     * @return object 值
     */
    public static String findMethodAnnotationValue(MethodDoc methodDoc, Class annotationClass,String name) {
        return findAnnotationValue(methodDoc.annotations(),annotationClass,name);
    }

    /**
     * 获取类上指定注解对应字段值
     * @param classDoc 类
     * @param annotationClass 指定注解
     * @param name 字段
     * @return object 值
     */
    public static String findClassAnnotationValue(ClassDoc classDoc, Class annotationClass,String name) {
        return findAnnotationValue(classDoc.annotations(),annotationClass,name);
    }

    /**
     * 获取参数指定注解字段值
     * @param param 参数注解
     * @param annotationClass 注解类型
     * @param fieldName 字段
     * @return object 值
     */
    public static String findParamAnnotionValue(Parameter param,Class annotationClass,String fieldName) {
        AnnotationDesc[] annotations = param.annotations();
        return findAnnotationValue(annotations,annotationClass,fieldName);
    }

    private static String findAnnotationValue(AnnotationDesc[] annotationDescs, Class annotationClass,String fieldName){
        for (AnnotationDesc annotationDesc : annotationDescs) {
            if (isEqual(annotationDesc,annotationClass)) {
                for (AnnotationDesc.ElementValuePair valuePair : annotationDesc.elementValues()) {
                    if (valuePair.element().name().equals(fieldName)) {
                        return valuePair.value().toString();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isEqual(AnnotationDesc annotationDesc,Class annotationClass){
        return annotationDesc.annotationType().qualifiedTypeName().equals(annotationClass.getName());
    }

    /**
     * 是否有@NotNull注解
     * @return boolean
     */
    public static boolean isHaveNotNull(FieldDoc fieldDoc) {
        AnnotationDesc[] annotations = fieldDoc.annotations();
        for (AnnotationDesc annotation : annotations) {
            if (isEqual(annotation,NotNull.class)) {
                return true;
            }
        }
        return false;
    }


    public static String jsonPropertyValue(FieldDoc fieldDoc) {
        for (AnnotationDesc annotationDesc : fieldDoc.annotations()) {
            if (isEqual(annotationDesc, JsonProperty.class)) {
                return findAnnotationValue(fieldDoc.annotations(),JsonProperty.class,"value");
            }
        }
        return null;
    }

}
