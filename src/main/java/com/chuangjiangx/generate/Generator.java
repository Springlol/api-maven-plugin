package com.chuangjiangx.generate;

import com.chuangjiangx.model.ClassComment;

import java.util.List;

/**
 * @author Tzhou on 2017/8/22.
 */
public interface Generator {

    void generate(List<ClassComment> classComments);
}
