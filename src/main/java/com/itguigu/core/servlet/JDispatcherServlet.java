package com.itguigu.core.servlet;

import com.itguigu.core.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * *******************************************
 *
 * @description:
 * @author: by jgl
 * @date: 2018/1/7 0007 上午 11:44
 * @version:1.0 *******************************************
 */
public class JDispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String SCANNER_PACKAGE = "scanPackage";
    private static Properties pro = new Properties();
    private static List<String> classNames = new ArrayList<>();
    private static Map<String, Object> ioc = new HashMap<String, Object>();
    private static List<Handle> handlerMapping = new ArrayList<>();

    /**
     * 初始化<br>
     * 1、加载配置文件application.properties 代替xml <br>
     * 2、扫描所有相关的类 , ---拿到基础包路径，递归扫描 <br>
     * 3、把扫描到的类实例化放到IOC容器中去 (我们自己要写一个IOC容器 , 实际上是一个Map)<br>
     * 4、依赖注入，只要加了@ZAutowired注解的字段，不管它是私有的还是公有的，还是受保护的，我们都要给它强制赋值<br>
     * 5、获取用户的请求，根据所请求的url找到其对应的method，通过反射机制去调用<br>
     * ---HandlerMapping 把这样一个关系存放到HandlerMapping，实际上是一个Map<br>
     * 6、等待请求，把反射调用结果通过response写入到浏览器中
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、加载配置文件
        doLoanConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描所有相关的类-通过File扫描递归
        doScanner(pro.getProperty(SCANNER_PACKAGE));
        //3、将扫描到的类实例化并放到IOC容器中
        doInstance();
        //4、依赖注入
        doAutowired();
        //5、获取用户请求，根据请求url，找到对应的method
        initHandlerMapping();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Error,Details:" + Arrays.toString(e.getStackTrace()));
        }
    }


    /**
     * 请求转发
     * @param req
     * @param resp
     * @throws Exception
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        System.out.println("==url=="+url);
        Handle handle = getHandle(req);
        if (null ==handle){
            // 如果没有匹配上，返回404错误
            resp.getWriter().write("404 Not Found");
            return;
        }
        // 获取方法的参数列表
        Class<?>[] paramTypes = handle.method.getParameterTypes();
        // 保存所有需要自动赋值的参数值
        Object[] paramValues = new Object[paramTypes.length];
        // 首先通过活动request的参数列表
        // 获得自己定义的方法的参数
        Map<String,String[]> params = req.getParameterMap();
        for (Map.Entry<String,String[]> param:params.entrySet()){
            System.out.println("===params==="+Arrays.toString(param.getValue()));
            String value = Arrays.toString(param.getValue()).replaceAll("\\[", "").replaceAll("\\]", "");
            if (!handle.paramIndexMapping.containsKey(param.getKey())){
                 continue;
            }
            // 如果找到匹配的对象，则开始填充参数值
            Integer index = handle.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramTypes[index],value);
            
        }
         // 设置方法中的request和response对象
        Integer reqIndex = handle.paramIndexMapping.get(HttpServletRequest.class.getName());
        Integer respIndex = handle.paramIndexMapping.get(HttpServletResponse.class.getName());
        paramValues[reqIndex] =req;
        paramValues[respIndex] =resp;
        handle.method.invoke(handle.controller,paramValues);
    }

    /**
     * 通过请求获取相应的处理类
     * @param req
     * @return
     */
    private Handle getHandle(HttpServletRequest req) {
        if (handlerMapping.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        for (Handle handle:handlerMapping){
           try {
               Matcher matcher = handle.pattern.matcher(url);
               // 如果没有匹配上继续下一个匹配
               if (!matcher.matches()){
                   continue;
               }
               return handle;
           }catch (Exception e){
              throw e;
           }
        }
        return null;
    }

    /**
     * 加载配置类
     * @param configLocation
     */
    private void doLoanConfig(String configLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            pro.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                 e.printStackTrace();
            }
        }
    }

    /**
     * 扫描所有的包，映射到List容器中
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        System.out.println("==getResouce==="+this.getClass().getClassLoader().getResource("/"));
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                // target/classes目录下不止有.class文件，也有.java文件
                if (file.getName().endsWith(".class")) {
                    classNames.add(scanPackage + "." + file.getName().replace(".class", ""));
                }
            }
        }
    }

    /**
     * 实例化Bean，让入IOC容器
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                // 不是所有的类都需要初始化的
                if (clazz.isAnnotationPresent(JController.class)) {
                    String beanName = clazz.getSimpleName();
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(JService.class)) {
                    /**
                     * 1、如果自己起了名字，优先使用自己的名字进行装配并注入<br>
                     * 2、默认首字母小写（发生在不是接口的情况）<br>
                     * 3、如果注入类型是接口，则要自动找到其实现类的实例并注入
                     */
                    JService jService = clazz.getAnnotation(JService.class);
                    String beanName = jService.value();//如果设置了值，不等于""
                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, clazz.newInstance());
                    } else {
                        beanName = lowerFirst(clazz.getSimpleName());
                        ioc.put(beanName, clazz.newInstance());
                    }
                    //todo 存疑，这个类已经实现了Jservice注解，为什么还要getInterfaces？getInterfaces不是找的是接口父类么？
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> inter : interfaces) {
                        ioc.put(inter.getName(), clazz.newInstance());
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            System.out.println("==entryKey==="+entry.getKey()+"===entryValue=="+entry.getValue());
            for (Field field : fields) {
                if (!field.isAnnotationPresent(JAutowired.class)) {
                    continue;
                }
                JAutowired autowired = field.getAnnotation(JAutowired.class);
                // 如果注解加了自定义的名字
                String beanName = autowired.value().trim();
                // 通过声明接口注入
                if ("".equals(beanName)) {
                    //todo getType().getName()?
                    System.out.println("==filed.getType=="+field.getType()+"==getType().getName=="+field.getType().getName());
                    beanName = field.getType().getName();
                }
                // 不管你愿不愿意，只要加了ZAutowired注解的，强行初始化
                field.setAccessible(true);
                try {
                    Object obj = ioc.get(beanName);
                    field.set(entry.getValue(), obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * private void initHandlerMapping() {
	 *
	 * if (ioc.isEmpty()) { return; }
	 *
	 * for (Entry<String, Object> entry : ioc.entrySet()) { // 非常具有技术含量
	 *
	 * // 把所有的RequestMapping扫描出来，读取它的值，跟Method关联上，并且放入到handleMapping之中去。
	 *
	 * Class<?> clazz = entry.getValue().getClass();
	 *
	 * // 只跟JController if (!clazz.isAnnotationPresent(JController.class)) {
	 * continue; }
	 *
	 * String baseUrl = ""; if
	 * (clazz.isAnnotationPresent(JRequestMapping.class)) { JRequestMapping
	 * requestMapping = clazz .getAnnotation(JRequestMapping.class); baseUrl =
	 * requestMapping.value(); }
	 *
	 * Method[] methods = clazz.getMethods();
	 *
	 * for (Method method : methods) { if
	 * (!method.isAnnotationPresent(JRequestMapping.class)) { continue; }
	 *
	 * JRequestMapping requestMapping = method
	 * .getAnnotation(JRequestMapping.class); String mappingUrl = ("/" + baseUrl
	 * + requestMapping.value() .replaceAll("/+", "/"));
	 *
	 * handlerMapping.put(mappingUrl, method);
	 *
	 * System.out.println("Mapping: " + mappingUrl + ",Method: " + method); } }
	 * }
	 */

    /**
     *
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 非常具有技术含量
            // 把所有的RequestMapping扫描出来，读取它的值，跟Method关联上，并且放入到handleMapping之中去。
            Class<?> clazz = entry.getValue().getClass();
            //只去读JController
            if (!clazz.isAnnotationPresent(JController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(JRequestMapping.class)) {
                JRequestMapping jRequestMapping = clazz.getAnnotation(JRequestMapping.class);
                baseUrl = jRequestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(JRequestMapping.class)) {
                    continue;
                }
                JRequestMapping jRequestMapping = method.getAnnotation(JRequestMapping.class);
                String mappingUrl = ("/" + baseUrl +"/"+ jRequestMapping.value()).replaceAll("/+", "/");
                // 把url和Method的关系再重新封装一次
                Pattern pattern = Pattern.compile(mappingUrl);
                handlerMapping.add(new Handle(entry.getValue(), method, pattern));
                System.out.println("mapping " + mappingUrl + "," + method);
            }
        }
    }

    /**
     * Handle记录Controller中的RequestMapping和Method的对应关系
     */
    private class Handle {
        protected Object controller;
        protected Method method;
        protected Pattern pattern;//模仿Spring的url是支持正则的
        protected Map<String, Integer> paramIndexMapping;// 参数顺序

        protected Handle(Object controller, Method method, Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            paramIndexMapping = new HashMap<>();
            putParamIndexMapping(method);
        }

        // 提取方法中的接了注解的参数
        private void putParamIndexMapping(Method method) {
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof JRequestParam) {
                        String paramName = ((JRequestParam) annotation).value();
                        if (!"".equals(paramName)) {
                            paramIndexMapping.put(paramName, i);
                        }
                    }
                }
            }
            // 提取方法中的request和response参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }


        }
    }

    private Object convert(Class<?> type, String value) {
        if (Integer.class == type) {
            return Integer.valueOf(value);
        }
        return value;
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    public String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
