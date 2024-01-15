package com.mvc.context;

import com.mvc.annotation.Autowired;
import com.mvc.annotation.Controller;
import com.mvc.annotation.Service;
import com.mvc.handler.CusHandler;
import com.mvc.xml.XmlParser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表示自定义的Spring容器
 */
public class CusWebApplicationContext {

    //存放扫描包以及子包的类的全路径
    private List<String> classFullPathList = new ArrayList<String>();
    //定义容器
    public ConcurrentHashMap<String, Object> ioc = new ConcurrentHashMap<String, Object>();
    //从web.xml中获取的配置的spring配置文件名称
    private String contextConfigLocation;

    //前端控制器DispatcherServlet是随着Tomcat的启动时启动,在前端控制器DispatcherServlet启动的时候初始化容器
    //编写方法,完成自定义Spring容器初始化
    public void init() {
        //这里应该动态的从web.xml中动态的获取，在web.xml中有配置文件
        //String basePackage = XmlParser.getBasePackage("cusspringmvc.xml");
        String basePackage = XmlParser.getBasePackage(contextConfigLocation.split(":")[1]);
        String[] AllPackageArray = basePackage.split(",");
        if (AllPackageArray.length > 0) {
            for (String pack : AllPackageArray) {
                ScanBasePackage(pack);
            }
        }
        //注入到容器
        injectIOC();
        //自动装配
        executeAutoWired();
    }


    //创建方法,完成对包的扫描
    public void ScanBasePackage(String basePackagePath) {
        //获取该包所在的工作路径(绝对路径)
        basePackagePath = basePackagePath.replace(".", "/");
        ClassLoader classLoader = CusWebApplicationContext.class.getClassLoader();
        URL url = classLoader.getResource("/" + basePackagePath);


        //获取该路径下的所有文件,判断是不是文件夹.递归处理,把类的全路径保存起来
        String path = url.getFile();
        //在io中把目录也视为一个文件
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //如果是文件夹.则递归
                ScanBasePackage(basePackagePath + "/" + file.getName());
            } else {
                //不是文件夹，但是该文件不一定是class文件,也可能是其他文件，即便是class文件,也不能确定该class文件是不是要注入到容器中(检查是否存在注解)
                if (file.getName().endsWith(".class")) {
                    //目前先把所有的路径全部都保存到集合中.后面在注入对象到容器的时候在去判断是不是有注解
                    classFullPathList.add((basePackagePath + "/" + file.getName()).replaceAll(".class", ""));
                }
            }
        }
    }


    //根据路径反射实例,判断实例上是否有注解,符合的放到ioc容器中
    public void injectIOC() {
        if (classFullPathList.size() == 0) {
            return;
        } else {
            for (String s : classFullPathList) {
                s = s.replace("/", ".");
                //获取到类名
                String className = s.substring(s.lastIndexOf(".") + 1);
                try {
                    Class<?> aClass = Class.forName(s);
                    //判断有没有注解
                    if (aClass.isAnnotationPresent(Controller.class)) {
                        ClassLoader classLoader = aClass.getClassLoader();
                        Controller ControllerAnnotation = aClass.getAnnotation(Controller.class);
                        String value = ControllerAnnotation.value();
                        if (value != null && !value.equals("")) {
                            className = value;
                        }
                        //存在注解,注入到容器
                        //首字母小写
                        ioc.put(StringUtils.uncapitalize(className), classLoader.loadClass(s).newInstance());
                    } else if (aClass.isAnnotationPresent(Service.class)) {
                        //表示这些类被Service注解标识
                        //先获取到注解的value值
                        Service ServiceAnnotation = aClass.getAnnotation(Service.class);
                        String value = ServiceAnnotation.value();
                        if (value != null && !value.equals("")) {
                            className = value;
                        }

                        Object instance = aClass.newInstance();
                        //如果没有指定value
                        //可以通过接口名/类名(首字母小写)注入到容器
                        //1.反射得到所有接口的名称
                        Class<?>[] interfaces = aClass.getInterfaces();
                        for (Class<?> anInterface : interfaces) {
                            //通过多个接口名来注入(首字母小写)
                            //首字母小写
                            String beanName = anInterface.getSimpleName().substring(0, 1).toLowerCase() +
                                    anInterface.getSimpleName().substring(1);
                            ioc.put(beanName, instance);
                            ioc.put(className.substring(0,1).toLowerCase() + className.substring(1), instance);
                        }

                    }
                } catch (Exception e) {
                    System.out.println("发生了错误" + e);
                }
            }
        }
    }

    //完成属性的自动装配
    public void executeAutoWired() {
        //判断ioc有没有装配的对象
        if (ioc.isEmpty()) {
            return;
        } else {
            //遍历ioc容器中的bean,获取到bean的所有字段,判断是否有@Autowired注解需要装配
            Set<Map.Entry<String, Object>> entries = ioc.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                Class<?> aClass = entry.getValue().getClass();
                Object bean = entry.getValue();
                try {
                    Field[] declaredFields = aClass.getDeclaredFields();
                    if (declaredFields.length > 0) {
                        for (Field declaredField : declaredFields) {
                            if (declaredField.isAnnotationPresent(Autowired.class)) {
                                //标识了@Autowired注解,需要进行自动装配
                                //如果是私有的privite,需要爆破
                                declaredField.setAccessible(true);
                                //判断直接是否设置value
                                Autowired AutowiredAnnotation = declaredField.getAnnotation(Autowired.class);
                                String value = AutowiredAnnotation.value();
                                if ("".equals(value)) {
                                    //没有设置value
                                    //从容器中按照 字段类型的首字母小写 查找
                                    Class<?> type = declaredField.getType();
                                    String simpleName = type.getSimpleName().substring(0,1).toLowerCase() + type.getSimpleName().substring(1);
                                    Object o = ioc.get(simpleName);
                                    if (o == null) {
                                        throw new RuntimeException("ioc容器中不存在要自动装载的bean");
                                    }else {
                                        declaredField.set(bean,o);
                                    }
                                }else {
                                    //有设置value
                                    //从容器中按照 字段名 查找
                                    Object o = ioc.get(value);
                                    if (o == null) {
                                        //查找不到
                                        throw new RuntimeException("ioc容器中不存在要装配的bean");
                                    }else {
                                        declaredField.set(bean,o);
                                    }

                                }
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public CusWebApplicationContext() {
    }

    public CusWebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

}
