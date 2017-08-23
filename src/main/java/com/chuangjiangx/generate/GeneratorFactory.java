package com.chuangjiangx.generate;

import com.chuangjiangx.generate.impl.SpringMvcGenerator;

/**
 * @author Tzhou on 2017/8/22.
 */
public class GeneratorFactory {

    public static Generator mvcGenerator() {
        return new SpringMvcGenerator();
    }
}
