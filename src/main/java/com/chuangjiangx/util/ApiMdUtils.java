package com.chuangjiangx.util;

import com.chuangjiangx.model.ClassComment;
import com.chuangjiangx.model.FieldComment;
import com.chuangjiangx.model.MethodComment;

import java.util.Iterator;
import java.util.List;

/**
 * @author by zhoutao on 2017/11/2.
 */
public class ApiMdUtils {

    private static final String TAB = "    ";

    public static String getApiMdHeader(ClassComment classComment) {
        return "FORMAT: 1A" + "\n" +
                "# " + classComment.getComment() + "\n" +
                "\n" +
                "# Group " + classComment.getComment() + "\n" +
                "\n" +
                "--------------------------------------" + "\n" +
                "\n";
    }


    // ## 获取省市区  [POST /mybank-sign/list-region ]
    public static String getPostMethodUrl(MethodComment methodComment) {
        return "## " + methodComment.getComment() + "  [" + methodComment.getRequestMethod() + " " +
                methodComment.getUri() + " ]";
    }

    public static String getGetMethodUrl(MethodComment methodComment) {
        StringBuilder uri = new StringBuilder("## " + methodComment.getComment() + "  [GET " + methodComment.getUri());
        List<FieldComment> args = methodComment.getMethodArgumentComments();
        if (args != null && args.size() > 0) {
            uri.append("?");
            for (FieldComment arg : args) {
                uri.append(arg.getName()).append("=").append(arg.getArg()).append("&");
            }
            uri.deleteCharAt(uri.length() - 1);
        }
        uri.append(" ]");
        return uri.toString();
    }


    public static String getHeaders() {
        return TAB + "+ Headers";
    }


    public static String getUrlParameters(MethodComment methodComment) {
        StringBuilder sb = new StringBuilder("+ Parameters \n");
        sb.append("\n");
        List<FieldComment> args = methodComment.getMethodArgumentComments();
        if (args != null && args.size() > 0) {
            for (FieldComment arg : args) {
                sb.append(TAB).append("+ ").append(arg.getName()).append(" (")
                        .append(arg.getTypeName()).append(",").append(arg.isRequired() ? "required)" : "optional)")
                        .append("  - ").append(arg.getComment()).append("\n");
            }
        }
        return sb.toString();
    }

    // + Request (application/x-www-from-urlencoded)
    public static String getRequestContentType(MethodComment methodComment) {
        return "+ Request (" + methodComment.getReqContentType() + ") ";
    }


    public static String getBody(List<FieldComment> argComments) {
        if (argComments == null || argComments.size() == 0) {
            return "\n";
        }
        String body = TAB + "+ Body" + "\n" +
                "\n";
        body += getFieldJson(argComments, TAB + TAB);
        return body;
    }

    // + Response 200 (application/json)
    public static String getResponseContentType(MethodComment methodComment) {
        return "+ Response 200 (" + methodComment.getRespContentType() + ")";
    }

    private static String getFieldJson(List<FieldComment> fieldComments, String indent) {
        StringBuilder sb = new StringBuilder(indent + "{ \n");
        Iterator<FieldComment> iterator = fieldComments.iterator();
        while (iterator.hasNext()) {
            FieldComment fieldComment = iterator.next();
            sb.append(indent).append(TAB).append("\"").append(fieldComment.getName()).append("\" : ");
            String typeName = fieldComment.getTypeName();
            if ("string".equals(typeName)) {
                sb.append("\"").append(fieldComment.getArg()).append("\"");
            } else if ("object".equals(typeName)) {
                sb.append("\n");
                List<FieldComment> comments = fieldComment.getFieldComments();
                String fieldJson = getFieldJson(comments, indent + TAB + TAB);
                sb.append(fieldJson);
            } else if ("array".equals(typeName)) {
                sb.append("[").append("\n");
                String fieldJson = getFieldJson(fieldComment.getFieldComments(), indent + TAB + TAB);
                sb.append(fieldJson).append("\n");
                sb.append(indent).append(TAB).append("]");
            } else {
                sb.append(fieldComment.getArg());
            }
            if (iterator.hasNext()) {
                sb.append(",");
            }
            if (!("object".equals(typeName) || "array".equals(typeName))) {
                sb.append(" - ").append(fieldComment.getComment());
            }
            sb.append("\n");
        }
        sb.append(indent).append("}");
        return sb.toString();
    }

}
