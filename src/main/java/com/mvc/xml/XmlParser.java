package com.mvc.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;

//Xml文件解析工具类,用于解析xml
public class XmlParser {
    public static String getBasePackage(String xmlPath) {

        SAXReader saxReader = new SAXReader();
        //通过得到类的加载器 -> 获取到Spring配置文件
        InputStream inputStream = XmlParser.class.getClassLoader().getResourceAsStream(xmlPath);
        String basePackagePath;
        try {
            Document document = saxReader.read(inputStream);
            Element rootElement = document.getRootElement();
            Element element = rootElement.element("component-scan");
            basePackagePath = element.attributeValue("base-package");
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return basePackagePath;
    }
}
