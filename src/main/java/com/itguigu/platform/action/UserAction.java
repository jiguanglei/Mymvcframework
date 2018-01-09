package com.itguigu.platform.action;

import com.itguigu.core.annotation.JAutowired;
import com.itguigu.core.annotation.JController;
import com.itguigu.core.annotation.JRequestMapping;
import com.itguigu.core.annotation.JRequestParam;
import com.itguigu.platform.service.offerInterface.BaseService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 下午 12:03
 * @version:1.0
 *  *******************************************
 */
@JController
@JRequestMapping("/user")
public class UserAction {

    @JAutowired
    BaseService baseService;

    @JRequestMapping("query")
    public void query(HttpServletRequest request, HttpServletResponse response, @JRequestParam("username") String username){
        String name = baseService.get(username);
        try {
            response.getWriter().write(name);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JRequestMapping("/add")
    public void add(HttpServletRequest request,HttpServletResponse response,@JRequestParam("username") String username){
        try {
            response.getWriter().write("com.itguigu.platform.action.UserAction.add()-"+username);
        }catch (Exception e){

        }
    }
    @JRequestMapping("/update")
    public void update(HttpServletRequest request,HttpServletResponse response,@JRequestParam("username") String username){
        try {
            response.getWriter().write("com.itguigu.platform.action.UserAction.update()-"+username);
        }catch (Exception e){

        }
    }
    @JRequestMapping("/delete")
    public void delete(HttpServletRequest request,HttpServletResponse response,@JRequestParam("username") String username){
        try {
            response.getWriter().write("com.itguigu.platform.action.UserAction.delete()-"+username);
        }catch (Exception e){

        }
    }
}
