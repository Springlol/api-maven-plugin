package com.chuangjiangx.generate;

import com.chuangjiangx.generate.impl.StandardGenerator;
import com.chuangjiangx.generate.impl.WithAnnoGenerator;

/**
 * @author Tzhou on 2017/8/22.
 */
public class GeneratorFactory {

    public static Generator standardGenerator() {
        return new StandardGenerator();
    }

    public static Generator withAnnoGenerator() {
        return new WithAnnoGenerator();
    }
}
