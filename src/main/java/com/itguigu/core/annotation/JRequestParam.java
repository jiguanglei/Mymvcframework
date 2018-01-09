package com.itguigu.core.annotation;

import java.lang.annotation.*;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 上午 11:55
 * @version:1.0 *******************************************
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JRequestParam {
    String value() default "";
}
