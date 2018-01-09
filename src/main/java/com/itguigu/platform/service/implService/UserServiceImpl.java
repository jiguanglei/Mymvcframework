package com.itguigu.platform.service.implService;

import com.itguigu.core.annotation.JService;
import com.itguigu.platform.service.offerInterface.BaseService;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 上午 11:59
 * @version:1.0 *******************************************
 */
@JService
public class UserServiceImpl implements BaseService {
    @Override
    public String get(String name) {
        return "this is user service name is "+name;
    }
}
