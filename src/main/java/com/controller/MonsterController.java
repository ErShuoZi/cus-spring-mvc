package com.controller;

import com.entity.Monster;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvc.annotation.*;
import com.service.MonsterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    public String login(HttpServletRequest request,HttpServletResponse response,@RequestParam(value = "monsterName") String mname) {
        boolean login = monsterService.login(mname);
        request.setAttribute("mName",mname);
        if (login) {
            return "forward:/login_ok.jsp";

        } else {
            return "forward:/login_error.jsp";
        }
    }

    @RequestMapping(value = "/listmonsterbyjson")
    @ResponseBody //标识了该注解代表希望以json格式返回
    //目标方法返回的结果是给springmvc底层,通过反射调用的位置
    //我们在springmvc底层反射调用的位置.接收结果，解析
    public List<Monster> listMonsterByJson(HttpServletRequest request,HttpServletResponse response) {
        List<Monster> monsters = monsterService.listMonsters();
        System.out.println(monsters);
        return monsters;
    }


    public MonsterService getMonsterService() {
        return monsterService;
    }

    public void setMonsterService(MonsterService monsterService) {
        this.monsterService = monsterService;
    }
}
