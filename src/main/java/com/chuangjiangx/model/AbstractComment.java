package com.chuangjiangx.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author  by Tzhou on 2017/8/19.
 */
@Getter
@Setter
public abstract class AbstractComment {

    /**
     * 注释
     */
    private String comment;

    /**
     * 原始注释
     */
    private String rawComment;

}
