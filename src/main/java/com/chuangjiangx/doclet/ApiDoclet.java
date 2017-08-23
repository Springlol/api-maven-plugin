package com.chuangjiangx.doclet;

import com.chuangjiangx.introspect.IntrospectFactory;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;

/**
 * Created by Tzhou on 2017/8/19.
 * doclet 生成入口类
 */
@SuppressWarnings("unused")
public class ApiDoclet extends Standard {

    public static boolean start(RootDoc root) {
        IntrospectFactory.springMvc().introspect(root);
        return true;
    }

    /**
     * 检查选项
     *
     * @param option 附加选项
     */
    public static int optionLength(String option) {
        return option.split(" ").length;
    }

    /**
     * 验证参数有效性
     *
     * @param options  参数
     * @param reporter 参数报告器
     */
    public static boolean validOptions(String [][] options, DocErrorReporter reporter) {
        return true;
    }

}
