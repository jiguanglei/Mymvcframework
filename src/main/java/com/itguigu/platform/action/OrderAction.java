package com.itguigu.platform.action;

import com.itguigu.core.annotation.JAutowired;
import com.itguigu.core.annotation.JController;
import com.itguigu.core.annotation.JRequestMapping;
import com.itguigu.core.annotation.JRequestParam;
import com.itguigu.platform.service.implService.OrderServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 下午 12:04
 * @version:1.0 *******************************************
 */
@JController
@JRequestMapping("/order")
public class OrderAction {
    @JAutowired("orderService")
    OrderServiceImpl orderService;

    @JRequestMapping("/edit.json")
    public void edit(HttpServletRequest request, HttpServletResponse response, @JRequestParam("ordername") String ordername){
        try {
            String name = orderService.get(ordername);
            response.getWriter().write(name);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
