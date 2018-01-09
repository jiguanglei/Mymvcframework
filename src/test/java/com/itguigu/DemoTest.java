package com.itguigu;

import org.junit.Test;

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
 * @date: 2018/1/7 0007 下午 2:00
 * @version:1.0 *******************************************
 */
public class DemoTest {

    @Test
    public void test01() throws  Exception{
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
