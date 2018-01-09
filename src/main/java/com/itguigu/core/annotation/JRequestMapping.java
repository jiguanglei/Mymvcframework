package com.itguigu.core.annotation;

import java.lang.annotation.*;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 上午 11:54
 * @version:1.0 *******************************************
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JRequestMapping {
    String value() default "";
}
