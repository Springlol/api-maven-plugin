package com.chuangjiangx.introspect.impl;

import static com.chuangjiangx.util.DocUtils.findMethodAnnotationValue;
import static com.chuangjiangx.util.DocUtils.findValidClass;
import static com.chuangjiangx.util.DocUtils.findValidMethod;

import com.chuangjiangx.generate.GeneratorFactory;
import com.chuangjiangx.generate.MdType;
import com.chuangjiangx.introspect.Introspect;
import com.chuangjiangx.model.ClassComment;
import com.chuangjiangx.model.FieldComment;
import com.chuangjiangx.model.MethodComment;
import com.chuangjiangx.util.ContextUtil;
import com.chuangjiangx.util.ParamUtils;
import com.chuangjiangx.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tzhou on 2017/8/19.
 * springMvc API 生成文档
 */
@Slf4j
public class SpringMvcIntrospect implements Introspect {
    @Override
    public void introspect(RootDoc rootDoc) {
        List<ClassDoc> classDocs = findValidClass(rootDoc, Controller.class, RestController.class);
        List<ClassComment> classCommentList = new ArrayList<>();

        for (ClassDoc classDoc : classDocs) {
            ClassComment classComment = new ClassComment();
            classComment.inspectClass(classDoc);

            //处理method
            List<MethodDoc> methodDocs = findValidMethod(classDoc, RequestMapping.class);
            List<MethodComment> methodCommentList = new ArrayList<>();
            for (MethodDoc methodDoc : methodDocs) {
                Object o = ContextUtil.get(ContextUtil.FILTER_METHODS_KEY);
                if (o != null && o instanceof List) {
                    List methods = (List) o;
                    if (methods.size() > 0 && (! methods.contains(methodDoc.name()))) {
                        continue;
                    }
                }
                MethodComment methodComment = new MethodComment();
                methodComment.inspectMethod(methodDoc);
                String classCommentRequestMapping = classComment.getRequestMapping();
                String methodCommentRequestMapping = methodComment.getRequestMapping();
                if (StringUtils.isBlank(classCommentRequestMapping)) {
                    methodComment.setUri(methodCommentRequestMapping);
                } else {
                    methodComment.setUri(classCommentRequestMapping + methodCommentRequestMapping);
                }
                methodComment.setReqContentType(ParamUtils.detectContentType(methodDoc));
                //处理形参
                List<FieldComment> comments = ParamUtils.inspectParam(rootDoc, methodDoc);
                methodComment.setMethodArgumentComments(comments);
                //处理返回值
                FieldComment fieldComment = ParamUtils.inspectReturn(rootDoc,methodDoc);
                String produces = StringUtils.replaceQuotes(
                        findMethodAnnotationValue(methodDoc, RequestMapping.class, "produces"));
                methodComment.setRespContentType(produces == null ? classComment.getProduces() : produces);
                methodComment.setMethodReturnComment(fieldComment);

                methodCommentList.add(methodComment);
            }
            classComment.setMethodComments(methodCommentList);
            classCommentList.add(classComment);
        }

        log.info(JSON.toJSONString(classCommentList));
        //生成文档
        MdType mdType = (MdType) ContextUtil.get(ContextUtil.MDTYPE_KEY);
        switch (mdType) {
            case STANDARD:
                GeneratorFactory.standardGenerator().generate(classCommentList);
                break;
            case WITH_ANNO:
                GeneratorFactory.withAnnoGenerator().generate(classCommentList);
                break;
            default:
                GeneratorFactory.withAnnoGenerator().generate(classCommentList);
        }
    }

}
