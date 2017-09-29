package com.chuangjiangx.generate.impl;

import com.chuangjiangx.generate.Generator;
import com.chuangjiangx.model.ClassComment;
import com.chuangjiangx.model.FieldComment;
import com.chuangjiangx.model.MethodComment;
import com.chuangjiangx.util.ContextUtil;
import com.chuangjiangx.util.StringUtils;
import com.chuangjiangx.util.TypeUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * 生成md文档
 *
 * @author Tzhou on 2017/8/22.
 */
@Slf4j
public class SpringMvcGenerator implements Generator {
    private static final String TAB = "    ";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void generate(List<ClassComment> classComments) {
        for (ClassComment classComment : classComments) {
            File filePath = new File(ContextUtil.get(ContextUtil.OUTPUT_KEY));
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            File mdFile = new File(filePath, classComment.getClassName() + ".md");
            StringBuilder sb = new StringBuilder();
            sb.append("FORMAT: 1A").append("\n");
            sb.append("# ").append(classComment.getComment()).append("\n");
            sb.append("\n");
            sb.append("# Group ").append(classComment.getComment()).append("\n");
            sb.append("\n");
            sb.append("--------------------------------------").append("\n");
            sb.append("\n");

            for (MethodComment methodComment : classComment.getMethodComments()) {
                executeRequest(methodComment, sb);
                executeResponse(methodComment, sb);
            }

            try (FileOutputStream fos = new FileOutputStream(mdFile);
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
            ) {
                bw.write(sb.toString());
                Runtime.getRuntime().exec("CMD /c aglio -i "
                        + mdFile.getAbsolutePath() + " -o " + mdFile.getParentFile().getAbsolutePath()
                        + "\\" + classComment.getClassName() + ".html");
            } catch (Exception ex) {
                log.error("解析生成MD文件异常");
            }
        }
    }


    private void executeRequest(MethodComment methodComment, StringBuilder sb) {
        String tab;
        if (methodComment.getRequestMethod().equals("GET")) {
            tab = executeGetMethod(methodComment, sb);
        } else {
            tab = executePostMethod(methodComment, sb);
        }
        //request对象处理
        if (!methodComment.isRest()) {
            for (FieldComment comment : methodComment.getMethodArgumentComments()) {
                executeNestedField(sb, comment, tab);
            }
        }
        sb.append("\n");
    }

    //返回值对象处理
    private void executeResponse(MethodComment methodComment, StringBuilder sb) {
        FieldComment returnComment = methodComment.getMethodReturnComment();
        if (returnComment == null
                || "void".equals(returnComment.getTypeName())
                || TypeUtils.isCommonType(returnComment.getTypeName())) {
            sb.append("+ Response 200 (application/json)").append("\n");
            sb.append("\n");
            return;
        }
        String contentType = methodComment.getRespContentType();
        contentType = contentType == null ? "application/json" : contentType;

        sb.append("+ Response 200 (").append(contentType).append(") \n");
        sb.append("\n");
        sb.append("    + Attributes").append("\n");

        if (returnComment.getFieldComments() == null || returnComment.getFieldComments().size() == 0) {
            //基本类型
            executeNestedField(sb, returnComment, TAB + TAB);
        } else {
            List<FieldComment> fieldComments = returnComment.getFieldComments();
            for (FieldComment fieldComment : fieldComments) {
                executeNestedField(sb, fieldComment, TAB + TAB);
            }
        }
        sb.append("\n");
    }

    private String executePostMethod(MethodComment methodComment, StringBuilder sb) {
        String[] linesSplit = StringUtils.linesSplit(methodComment.getComment());
        String isp = getMethodComment(linesSplit);
        sb.append("## ").append(isp).append("  [")
                .append(methodComment.getRequestMethod()).append(" ")
                .append(methodComment.getUri());
        sb.append(" ] \n");
        if (linesSplit != null && linesSplit.length > 1) {
            sb.append(linesSplit[1]).append("\n");
        }
        sb.append("\n");
        if (methodComment.isRest()) {
            executeRest(methodComment, sb);
            sb.append("+ Request (").append(methodComment.getReqContentType()).append(") \n");
            sb.append("\n");
        } else {
            sb.append("+ Request (").append(methodComment.getReqContentType()).append(") \n");
            sb.append("\n");
            if (methodComment.getMethodArgumentComments().size() > 0) {
                sb.append("    + Attributes").append("\n");
            }
        }
        return TAB + TAB;
    }

    private void executeRest(MethodComment methodComment, StringBuilder sb) {
        if (methodComment.isRest()) {
            sb.append("+ Parameters").append("\n");
            sb.append("\n");
            List<FieldComment> argumentComments = methodComment.getMethodArgumentComments();
            for (FieldComment argumentComment : argumentComments) {
                sb.append(TAB).append("+ ").append(argumentComment.getName()).append(":`")
                        .append(argumentComment.getArg()).append("` (")
                        .append(argumentComment.getTypeName()).append(") -")
                        .append(argumentComment.getComment()).append("\n");
                sb.append("\n");
            }
        }
    }


    private String executeGetMethod(MethodComment methodComment, StringBuilder sb) {
        String[] linesSplit = StringUtils.linesSplit(methodComment.getComment());
        String isp = getMethodComment(linesSplit);
        sb.append("## ").append(isp).append("  [")
                .append(methodComment.getRequestMethod()).append(" ")
                .append(methodComment.getUri());
        if (methodComment.getMethodArgumentComments().size() > 0) {
            sb.append("{?");
            for (FieldComment comment : methodComment.getMethodArgumentComments()) {
                sb.append(comment.getName()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("}]\n");
            if (linesSplit != null && linesSplit.length > 1) {
                sb.append(linesSplit[1]).append("\n");
            }
            sb.append("\n");
            sb.append("+ Parameters");
        }
        sb.append("\n \n");
        return TAB;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String getMethodComment(String[] linesSplit) {
        String isp;
        if (linesSplit != null && linesSplit.length > 0) {
            isp = linesSplit[0];
            isp = isp.replace("(", "（");
            isp = isp.replace(")", "）");
        } else {
            isp = "请完善注释信息";
        }
        return isp;
    }

    //生成嵌套对象
    private void executeNestedField(StringBuilder sb, FieldComment fieldComment, String tab) {
        List<FieldComment> comments = fieldComment.getFieldComments();
        sb.append(tab).append("+ ");

        if (comments == null || comments.size() == 0) {  //基本类型
            sb.append(fieldComment.getName()).append(":`").append(fieldComment.getArg()).append("` (")
                    .append(fieldComment.getTypeName()).append(",")
                    .append(fieldComment.isRequired() ? "required" : "optional")
                    .append(") - ").append(fieldComment.getComment()).append("\n");
        } else {
            if ("object".equals(fieldComment.getTypeName())) {
                sb.append(fieldComment.getName()).append(" (object, ")
                        .append(fieldComment.isRequired() ? "required)" : "optional)").append(" - ")
                        .append(fieldComment.getComment()).append("\n");
                for (FieldComment comment : comments) {
                    executeNestedField(sb, comment, tab + TAB);
                }
            } else if ("array".equals(fieldComment.getTypeName())) {
                sb.append(fieldComment.getName()).append(" (array, ")
                        .append(fieldComment.isRequired() ? "required)" : "optional)").append(" - ")
                        .append(fieldComment.getComment()).append("\n");
                sb.append(tab).append(TAB);
                sb.append("+ (object) \n");
                for (FieldComment comment : comments) {
                    executeNestedField(sb, comment, tab + TAB + TAB);
                }
            }
        }
    }
}
