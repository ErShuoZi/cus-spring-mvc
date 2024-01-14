package com.mvc.annotation;

import java.lang.annotation.*;

//自定义注解代替原生的@Controller
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value()default "";
}
