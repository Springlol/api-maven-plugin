package com.chuangjiangx.introspect;

import com.chuangjiangx.introspect.impl.SpringMvcIntrospect;

/**
 * @author  by Tzhou on 2017/8/19.
 */
public class IntrospectFactory {

    public static Introspect springMvc() {
        return new SpringMvcIntrospect();
    }

}
