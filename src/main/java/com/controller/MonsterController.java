package com.controller;

import com.entity.Monster;
import com.mvc.annotation.Autowired;
import com.mvc.annotation.Controller;
import com.mvc.annotation.RequestMapping;
import com.mvc.annotation.RequestParam;
import com.service.MonsterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class MonsterController {
    @Autowired
    private MonsterService monsterService;

    //编写方法,可以列出Monster List
    //Spring Mvc 支持原生的servlet api,所以这里会有HttpServletRequest request, HttpServletResponse response 这两个形参
    @RequestMapping(value = "/list/monster")
    public void listMonsters(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        //获取writter 返回信息
        try {

            PrintWriter writer = response.getWriter();
            writer.print("<h1>Monster List Info</h1>");
            writer.print("<h1>" + monsterService.listMonsters() +
                    "</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @RequestMapping(value = "/monster/find")
    //@RequestParam(value = "name")
    public void findMonsterByName(HttpServletRequest request, HttpServletResponse response, String name) {
        ;
        List<Monster> findedMonsterList = monsterService.findMonsterByName(name);
        for (Monster monster : findedMonsterList) {
            System.out.println(monster);
        }
    }


    //处理登录方法
    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request,HttpServletResponse response,String mname) {
        boolean login = monsterService.login(mname);
        request.setAttribute("mName",mname);
        if (login) {
            return "forward:/login_ok.jsp";

        } else {
            return "forward:/login_error.jsp";
        }
    }


    public MonsterService getMonsterService() {
        return monsterService;
    }

    public void setMonsterService(MonsterService monsterService) {
        this.monsterService = monsterService;
    }
}
