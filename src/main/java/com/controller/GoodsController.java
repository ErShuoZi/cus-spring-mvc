package com.controller;

import com.mvc.annotation.Controller;
import com.mvc.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class GoodsController {

    @RequestMapping(value = "/list/goods")
    public void listGoods(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        //获取writter 返回信息
        try {
            PrintWriter writer = response.getWriter();
            writer.print("<h1>Goods List Info</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
