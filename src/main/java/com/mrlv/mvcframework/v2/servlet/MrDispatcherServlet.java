package com.mrlv.mvcframework.v2.servlet;



import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created by mrLv
 */
public class MrDispatcherServlet extends HttpServlet{

    /**
     * 保存application.properties配置文件中的内容
     * */
    private Properties contextConfig = new Properties();

    /**
     * 保存扫描的所有的类名
     * */
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


    /**
     *初始化阶段
     * */
    @Override
    public void init(ServletConfig config) throws ServletException {

        //加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        
        //初始化扫描到的类，并且将它们放入到ICO容器之中
        doInstance();
        
        //完成依赖注入
        doAutowired();

        //初始化HandlerMapping
        initHandlerMapping();

        System.out.println("GP Spring framework is init.");

    }



    private void initHandlerMapping() {

    }


    private void doAutowired() {


    }

    /**
     * 初始化扫描到的类，并且将它们放入到ICO容器之中
     * */
    private void doInstance() {
        
    }


    /**
    * 扫描出相关的类
    * */
    private void doScanner(String scanPackage) {
        //scanPackage中存储的是包路径，将包路径转换为文件路径
        URL url = this.getClass().getClassLoader().getResource("/." + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace(".class",""));
                classNames.add(className);
            }
        }
    }


    /**
     * 加载配置文件
     * */
    private void doLoadConfig(String contextConfigLocation) {
        //从类路径下找到Spring的主配置文件所在的路径，将其取出存放到properties对象中
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null == fis){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
