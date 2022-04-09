package com.mrlv.mvcframework.v2.servlet;



import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Tom.
 */
public class MrDispatcherServlet extends HttpServlet{

    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    //保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    //传说中的IOC容器，我们来揭开它的神秘面纱
    //为了简化程序，暂时不考虑ConcurrentHashMap
    // 主要还是关注设计思想和原理
    private Map<String,Object> ioc = new HashMap<String,Object>();

    //保存url和Method的对应关系
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {



    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {





    }


    //初始化阶段
    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        
        //3、初始化扫描到的类，并且将它们放入到ICO容器之中
        doInstance();
        
        //4、完成依赖注入
        doAutowired();

        //5、初始化HandlerMapping
        initHandlerMapping();

        System.out.println("GP Spring framework is init.");

    }


    //初始化url和Method的一对一对应关系
    private void initHandlerMapping() {

    }

    //自动依赖注入
    private void doAutowired() {


    }

    private void doInstance() {

    }


    //扫描出相关的类
    private void doScanner(String scanPackage) {

    }


    //加载配置文件
    private void doLoadConfig(String contextConfigLocation) {

    }
}
