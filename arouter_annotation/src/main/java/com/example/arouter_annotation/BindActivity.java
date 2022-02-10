package com.example.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Desc:
 * @Author: puyantao
 * @CreateDate: 2022/2/10 11:39
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BindActivity {
    String value();
}
