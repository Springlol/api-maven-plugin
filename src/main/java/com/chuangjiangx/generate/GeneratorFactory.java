package com.chuangjiangx.generate;

import com.chuangjiangx.generate.impl.StandardGenerator;
import com.chuangjiangx.generate.impl.WithAnnoGenerator;
import com.chuangjiangx.model.ClassComment;

import java.util.List;

/**
 * @author Tzhou on 2017/8/22.
 */
public class GeneratorFactory {

    private Generator generator;

    public GeneratorFactory(Generator generator) {
        this.generator = generator;
    }

    public void generate(List<ClassComment> classComments) {
        generator.generate(classComments);
    }


    public static Generator standardGenerator() {
        return new StandardGenerator();
    }

    public static Generator withAnnoGenerator() {
        return new WithAnnoGenerator();
    }
}
