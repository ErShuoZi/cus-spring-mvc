package com.test;

import com.mvc.context.CusWebApplicationContext;
import com.mvc.xml.XmlParser;
import org.junit.Test;

public class SpringMvcTest {
    @Test
    public void readXML() {
        String basePackage = XmlParser.getBasePackage("cusspringmvc.xml");
        System.out.println(basePackage);
    }
}
