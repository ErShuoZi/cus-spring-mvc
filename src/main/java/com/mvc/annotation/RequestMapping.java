package com.mvc.annotation;

import java.lang.annotation.*;

//代替原生的@RequestMapping,指定控制器-方法的映射路径
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value()default "";
}
