package com.chuangjiangx.generate.impl;

import com.chuangjiangx.generate.AbstractGenerator;
import com.chuangjiangx.model.ClassComment;
import com.chuangjiangx.model.FieldComment;
import com.chuangjiangx.model.MethodComment;
import com.chuangjiangx.util.ApiMdUtils;

import java.util.List;

/**
 * @author by zhoutao on 2017/11/2.
 */
public class WithAnnoGenerator extends AbstractGenerator {

    @Override
    protected StringBuilder generateStringBuilder(ClassComment classComment) {
        StringBuilder sb = new StringBuilder(ApiMdUtils.getApiMdHeader(classComment));
        List<MethodComment> methodComments = classComment.getMethodComments();
        if (methodComments != null && methodComments.size() > 0) {
            for (MethodComment methodComment : methodComments) {
                sb.append(ApiMdUtils.getMethodUrl(methodComment)).append("\n");
                sb.append("\n");
                sb.append(ApiMdUtils.getRequestContentType(methodComment)).append("\n");
                sb.append("\n");
                sb.append(ApiMdUtils.getBody(methodComment.getMethodArgumentComments())).append("\n");
                sb.append("\n");
                sb.append(ApiMdUtils.getResponseContentType(methodComment)).append("\n");
                sb.append("\n");
                FieldComment comment = methodComment.getMethodReturnComment();
                sb.append(ApiMdUtils.getBody(comment.getFieldComments())).append("\n");
                sb.append("\n");
            }
        }
        return sb;
    }
}
