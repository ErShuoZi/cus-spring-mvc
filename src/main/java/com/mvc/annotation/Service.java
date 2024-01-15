package com.mvc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented //生成文档的时候携带该注解
//用于标识Service对象,注入到Spring容器
public @interface Service {
    String value()default "";
}
