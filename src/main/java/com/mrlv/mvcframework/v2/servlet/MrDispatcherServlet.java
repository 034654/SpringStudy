package com.mrlv.mvcframework.v2.servlet;



import com.mrlv.mvcframework.annotation.MrAutowired;
import com.mrlv.mvcframework.annotation.MrController;
import com.mrlv.mvcframework.annotation.MrRequestMapping;
import com.mrlv.mvcframework.annotation.MrService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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

    /**
     * 定义一个IOC容器用来存放初始化的对象
     * */
    private Map<String,Object> ioc = new HashMap<String,Object>();

    /**
     * 保存url和Method的对应关系
    * */
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Excetion ,Detail : " + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        //绝对路径
        String url = req.getRequestURI();
        //转化成相对路径
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!");
            return;
        }
        Method method =this.handlerMapping.get(url);

        //通过反射拿到method所在class，获取路径名，调用toLowerFirstCase获得beanName
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());

        Map<String,String []> params = req.getParameterMap();
        method.invoke(ioc.get(beanName),new Object[]{ req,resp,params.get("name")[0]});

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

        System.out.println("Mr Spring framework is successed.");

    }


    /**
     * 初始化URL和Method的一对一对应关系
     * */
    private void initHandlerMapping() {
        if(ioc.isEmpty()){
            return ;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MrController.class)){
                continue;
            }

            //保存写在类上变的Url  @MrRequestMapping("/demo")
            String baseUrl = "";
            if(clazz.isAnnotationPresent(MrRequestMapping.class)){
                MrRequestMapping requestMapping = clazz.getAnnotation(MrRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //默认获取所有的public方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(MrRequestMapping.class)){
                    continue;
                }
                MrRequestMapping requestMapping = clazz.getAnnotation(MrRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                            .replaceAll("/+" ,"/");
                handlerMapping.put(url,method);
                System.out.println("Mapping:" + url + "," + method);


            }


        }

    }

    /**
     * 自动的依赖注入
     * */
    private void doAutowired() {
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //根据反射机制获取类中的所有字段  Declared 所有的，特定的
           Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MrAutowired.class)){
                    continue;
                }
                MrAutowired autowired = field.getAnnotation(MrAutowired.class);
                String beanName = autowired.value().trim();
                //如果用户没有自定beanName，则使用类型注入
                if("".equals(beanName)){
                    //获取接口的类型作为IOC容器的Key
                    beanName = field.getType().getName();
                }

                //如果是public以外的的修饰符，只要加了@Autowired注解，都要强制赋值，反射中叫做强制赋值
                field.setAccessible(true);

                try {
                    //利用反射机制，动态给字段赋值
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }


    }

    /**
     * 初始化扫描到的类，并且将它们放入到ICO容器之中
     * */
    private void doInstance() {
        //初始化，为DI做准备
        if(classNames.isEmpty()){
            return ;
        }
        //遍历取出存在存在classNames中的文件
        try {
            for (String className: classNames ) {
                Class<?> clazz = Class.forName(className);
                //加类注解的类需要初始化
                if(clazz.isAnnotationPresent(MrController.class)){
                    //定义实例
                    Object instance = clazz.newInstance();
                    //将文件名的首字母转化成小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);
                }else if (clazz.isAnnotationPresent(MrService.class)){
                    //自定义的beanName,将service.value()值赋值给beanName
                    MrService service = clazz.getAnnotation(MrService.class);
                    String beanName = service.value();
                    //默认类名首字母小写，如果beanName为空，则将文件赋值给beanName
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    //定义实例
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    //根据类型自动赋值,遍历接口文件，获取接口的文件名，放入IOC容器中
                    for (Class<?> i : clazz.getInterfaces()) {
                        //如果IOC容器中接口的类名存在，需要抛出异常
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The" + i.getName() +"is exists!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    /**
     * 将文件名的首字母小写
     * */
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);
    }
}
