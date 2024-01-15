package com.controller;

import com.mvc.annotation.Autowired;
import com.mvc.annotation.Controller;
import com.mvc.annotation.RequestMapping;
import com.mvc.annotation.RequestParam;
import com.service.MonsterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
    //public void findMonsterByName(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "name") String  name)


    public MonsterService getMonsterService() {
        return monsterService;
    }

    public void setMonsterService(MonsterService monsterService) {
        this.monsterService = monsterService;
    }
}
