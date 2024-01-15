package com.mvc.servlet;

import com.entity.Monster;
import com.mvc.annotation.Controller;
import com.mvc.annotation.RequestMapping;
import com.mvc.annotation.RequestParam;
import com.mvc.context.CusWebApplicationContext;
import com.mvc.handler.CusHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 1.CusDispatcherServlet充当原生DispatcherServlet
 * 2.本质是Servlet，即继承HttpServlet
 */
public class CusDispatcherServlet extends HttpServlet {

    //定义属性HandlerList,保存CusHandler(URL和控制器方法的映射)
    private List<CusHandler> HandlerList = new ArrayList<CusHandler>();
    private CusWebApplicationContext cusWebApplicationContext = null;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //获取web.xml中的配置信息
        String contextConfigLocation = servletConfig.getInitParameter("contextConfigLocation");
        //前端控制器DispatcherServlet是随着Tomcat的启动时启动,在前端控制器DispatcherServlet启动的时候初始化容器
        //将配置的Spring配置文件名称传入
        cusWebApplicationContext = new CusWebApplicationContext(contextConfigLocation);
        cusWebApplicationContext.init();
        //完成url和控制器方法的映射
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调用方法完成请求分发
        executeDispatch(req, resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    //完成url和控制器方法的映射
    public void initHandlerMapping() {
        //当前ioc容器中是否为空？
        if (cusWebApplicationContext.ioc.isEmpty()) {
            return;
        } else {
            //不为空.遍历ioc容器，然后进行url映射
            //遍历map
            Set<Map.Entry<String, Object>> entries = cusWebApplicationContext.ioc.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                //先取出注入的Object的Class对象
                Class<?> aClass = entry.getValue().getClass();
                if (aClass.isAnnotationPresent(Controller.class)) {
                    //是Controller，获取该类的所有方法
                    Method[] declaredMethods = aClass.getDeclaredMethods();
                    //遍历方法
                    for (Method declaredMethod : declaredMethods) {
                        //再去判断方法上是否存在RequestMapping注解
                        if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                            //取出注解值
                            RequestMapping declaredAnnotation = declaredMethod.getDeclaredAnnotation(RequestMapping.class);
                            String url = declaredAnnotation.value();
                            //创建一个CusHandler对象,该对象就是一个映射关系了
                            CusHandler cusHandler = new CusHandler(url, entry.getValue(), declaredMethod);
                            //将该对象(映射关系)放入到集合
                            HandlerList.add(cusHandler);
                        }
                    }
                }
            }
        }
    }

    //通过请求request对象进行HandlerList查找,匹配则调用分发方法
    private CusHandler getCusHandler(HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();
        //遍历HandlerList
        for (CusHandler cusHandler : HandlerList) {
            if (cusHandler.getUrl().equals(requestURI)) {
                //匹配上了
                return cusHandler;
            }
        }
        return null;
    }


    //完成分发
    public void executeDispatch(HttpServletRequest request, HttpServletResponse response) {
        CusHandler cusHandler = getCusHandler(request, response);
        if (cusHandler != null) {
            //匹配成功，反射调用控制器的方法
            try {
                //这种写法是针对目标方法是
                //public void listMonsters(HttpServletRequest request, HttpServletResponse response)
                //但是要考虑到多参数的情况,将需要传递给目标方法的实参封装到数组,然后进行反射调用传入
                //   public Object invoke(Object obj, Object... args)支持...args方式
                //将HttpServletRequest 和 HttpServletResponse封装到参数数组
                //1.得到目标方法的参数信息(形参)
                Class<?>[] parameterTypes = cusHandler.getMethod().getParameterTypes();
                //2.定义实参数组
                Object[] params = new Object[parameterTypes.length];
                //3.遍历形参数组,根据形参数组信息,将实参填充到实参数组中
                //这里使用普通for,为的是得到对应的索引i
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    //如果该形参是HttpServletRequest,就request添加到params实参数组\
                    //在原生的SpringMVC中是根据类型判断，这里我做了简化
                    if ("HttpServletRequest".equals(parameterType.getSimpleName())) {
                        params[i] = request;
                    } else if ("HttpServletResponse".equals(parameterType.getSimpleName())) {
                        params[i] = response;
                    }
                }
                //将http请求参数封装到params实参数组中
                //要考虑实参的顺序问题

                //1.获取http请求的参数集合
                Map<String, String[]> parameterMap = request.getParameterMap();
                //2.遍历parameterMap将请求参数按照顺序填充到实参数组
                //返回的Map<String,String[]> String表示http的参数名；
                //String[]表示http请求参数值
                for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
                    //取出key(参数名)
                    String key = stringEntry.getKey();
                    //取出参数值(简化,目前不考虑多个参数的情况)
                    String s = stringEntry.getValue()[0];

                    //得到请求的参数对应目标方法的第几个形参,将其填充
                    //params[?] =
                    int indexRequestParameterIndex = getIndexRequestParameterIndex(cusHandler.getMethod(), key);
                    if (indexRequestParameterIndex != -1) {
                        //找到了目标方法中的形参对应的索引
                        params[indexRequestParameterIndex] = s;
                    } else {
                        //没有@RequestParmater注解,则使用默认机制进行配置
                        //判断请求参数的名称要和接收的形参的名称一致

                        //1.得到目标方法的所有形参的变量名称
                        //2.得到目标方法的所有形参名进行遍历,如果匹配当前的请求的参数名称,则填充到实参数组

                        List<String> allParamNameToList = getAllParamNameToList(cusHandler.getMethod());
                        for (int i = 0; i < allParamNameToList.size(); i++) {
                            if (key.equals(allParamNameToList.get(i))) {
                                params[i] = s;
                                break;
                            }
                        }


                    }

                }

                //cusHandler.getMethod().invoke(cusHandler.getController(), request, response);
                cusHandler.getMethod().invoke(cusHandler.getController(), params);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            //说明请求的资源不存在,返回404
            try {
                response.setContentType("text/html;charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.print("<h1>404 NOT FOUND</h1>");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * @param method 目标方法
     * @param name   请求的参数名
     * @return 是目标方法的第几个形参
     */
    public int getIndexRequestParameterIndex(Method method, String name) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            //判断parameter是否有@RequestParameter注解
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                //存在该注解
                RequestParam annotation = parameter.getAnnotation(RequestParam.class);
                String value = annotation.value();
                if (name.equals(value)) {
                    return i;//找到请求参数对应目标方法的形参的位置
                }
            }
        }
        return -1;
    }


    /**
     * @param method
     * @return
     */
    public List<String> getAllParamNameToList(Method method) {
        List<String> paramtersList = new ArrayList<>();
        //获取所有参数
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            //parameter.getName() 不是形参真正的名字,而是arg0 arg1 arg2...
            //解决这个问题需要配置idea编译 加入-paramter参数
            paramtersList.add(parameter.getName());
        }
        return paramtersList;
    }
}
