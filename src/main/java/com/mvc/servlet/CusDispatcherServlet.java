package com.mvc.servlet;

import com.mvc.annotation.Controller;
import com.mvc.annotation.RequestMapping;
import com.mvc.context.CusWebApplicationContext;
import com.mvc.handler.CusHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public void init() throws ServletException {
        //前端控制器DispatcherServlet是随着Tomcat的启动时启动,在前端控制器DispatcherServlet启动的时候初始化容器
        cusWebApplicationContext = new CusWebApplicationContext();
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
                cusHandler.getMethod().invoke(cusHandler.getController(), request, response);
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
}
