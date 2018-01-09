package com.itguigu.platform.service.implService;

import com.itguigu.core.annotation.JService;
import com.itguigu.platform.service.offerInterface.BaseService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 上午 11:59
 * @version:1.0 *******************************************
 */
@JService("orderService")
public class OrderServiceImpl implements BaseService {
    @Override
    public String get(String name) {
        return "this is order service name is "+name;
    }


    public static void main(String[] args) {
        new OrderServiceImpl().loadClass();
    }
    public void loadClass(){
        File file = new File("D:\\applicationContext.properties");
        try {
            InputStream inputStream = new FileInputStream(file);
            System.out.println("==inputstream=="+inputStream);
            Properties pro = new Properties();
            pro.load(inputStream);
            String scanPackage = pro.getProperty("scanPackage");
            URL resource = this.getClass().getClassLoader().getResource("");
            System.out.println("resource="+resource);
            URL resource1 = this.getClass().getClassLoader().getResource("/");
            System.out.println("resource/="+resource1);
            URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
            System.out.println("===URL=="+url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
