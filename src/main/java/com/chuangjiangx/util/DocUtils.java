package com.chuangjiangx.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * @author Tzhou on 2017/8/19.
 */
@SuppressWarnings("all")
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

    /**
     * 获取方法指定注解字段值
     *
     * @param methodDoc       方法
     * @param annotationClass 指定注解
     * @param name            字段
     * @return object 值
     */
    public static String findMethodAnnotationValue(MethodDoc methodDoc, Class annotationClass, String name) {
        return findAnnotationValue(methodDoc.annotations(), annotationClass, name);
    }

    /**
     * 获取类上指定注解对应字段值
     *
     * @param classDoc        类
     * @param annotationClass 指定注解
     * @param name            字段
     * @return object 值
     */
    public static String findClassAnnotationValue(ClassDoc classDoc, Class annotationClass, String name) {
        return findAnnotationValue(classDoc.annotations(), annotationClass, name);
    }

    /**
     * 获取参数指定注解字段值
     *
     * @param param           参数注解
     * @param annotationClass 注解类型
     * @param fieldName       字段
     * @return object 值
     */
    public static String findParamAnnotionValue(Parameter param, Class annotationClass, String fieldName) {
        AnnotationDesc[] annotations = param.annotations();
        return findAnnotationValue(annotations, annotationClass, fieldName);
    }

    private static String findAnnotationValue(AnnotationDesc[] annotationDescs, Class annotationClass, String fieldName) {
        for (AnnotationDesc annotationDesc : annotationDescs) {
            if (isEqual(annotationDesc, annotationClass)) {
                for (AnnotationDesc.ElementValuePair valuePair : annotationDesc.elementValues()) {
                    if (valuePair.element().name().equals(fieldName)) {
                        return valuePair.value().toString();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isEqual(AnnotationDesc annotationDesc, Class annotationClass) {
        return annotationDesc.annotationType().qualifiedTypeName().equals(annotationClass.getName());
    }

    /**
     * 是否有@NotNull注解
     *
     * @return boolean
     */
    public static boolean isHaveNotNull(FieldDoc fieldDoc) {
        AnnotationDesc[] annotations = fieldDoc.annotations();
        for (AnnotationDesc annotation : annotations) {
            if (isEqual(annotation, NotNull.class)) {
                return true;
            }
        }
        return false;
    }


    public static String jsonPropertyValue(FieldDoc fieldDoc) {
        for (AnnotationDesc annotationDesc : fieldDoc.annotations()) {
            if (isEqual(annotationDesc, JsonProperty.class)) {
                return findAnnotationValue(fieldDoc.annotations(), JsonProperty.class, "value");
            }
        }
        return null;
    }

    /**
     * 是否有指定类型注解
     */
    public static boolean isHaveAnno(ProgramElementDoc elementDoc, Class annoClass) {
        AnnotationDesc[] annotations = elementDoc.annotations();
        for (AnnotationDesc annotation : annotations) {
            if (isEqual(annotation, annoClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否有指定类型注解
     */
    public static boolean isHaveAnno(Parameter param,Class annoClass) {
        AnnotationDesc[] annotations = param.annotations();
        for (AnnotationDesc annotation : annotations) {
            if (isEqual(annotation,annoClass)) {
                return true;
            }
        }
        return false;
    }

}
