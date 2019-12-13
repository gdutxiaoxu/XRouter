package com.xj.xrouter.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 博客地址：http://blog.csdn.net/gdutxiaoxu
 * @author xujun
 *
 */
@Retention(RetentionPolicy.CLASS)
public @interface Modules {
    String[] value();
}
