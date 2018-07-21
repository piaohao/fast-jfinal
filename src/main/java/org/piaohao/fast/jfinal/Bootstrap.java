package org.piaohao.fast.jfinal;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.core.JFinalFilter;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.FixContextListener;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

/**
 * 启动器
 *
 * @author piaohao
 */
@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        run(args);
    }

    public static void run(String[] args) {
        DefaultConfig.init();
        String serverType = DefaultConfig.serverType;
        if (StrUtil.isBlank(serverType)) {
            runUndertow(args);
            return;
        }
        if (serverType.equals(Util.ServerType.TOMCAT.name())) {
            runTomcat(args);
        } else if (serverType.equals(Util.ServerType.UNDERTOW.name())) {
            runUndertow(args);
        } else {
            runUndertow(args);
        }
    }

    public static void runTomcat(String[] args) {
        long start = System.currentTimeMillis();
        Tomcat tomcat = new Tomcat();
        Integer serverPort = DefaultConfig.serverPort;
        tomcat.setBaseDir(DefaultConfig.tomcatBaseDir);
        tomcat.setPort(serverPort);

        String contextPath = DefaultConfig.contextPath;
        Context context = null;
        context = tomcat.addContext(contextPath, ClassUtil.getClassPath());
        context.setName("jfinal");
        context.addLifecycleListener(new FixContextListener());
        {
            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName("jfinal");
            filterDef.setFilter(new JFinalFilter());
            String configClass = DefaultConfig.configClass;
            if (StrUtil.isBlank(configClass)) {
                log.warn("请配置JFinalConfig");
            }
            filterDef.addInitParameter("configClass", configClass);
            context.addFilterDef(filterDef);
        }
        {
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName("jfinal");
            filterMap.addURLPattern("/*");
            context.addFilterMap(filterMap);
        }
        {
            StandardRoot standardRoot = new StandardRoot();
            context.setResources(standardRoot);
        }
        tomcat.getHost().addChild(context);
        tomcat.addServlet(contextPath, "defaultServlet", new DefaultServlet());
        context.addServletMappingDecoded("/*", "defaultServlet");

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            log.error("tomcat启动失败!", e);
            System.exit(-1);
        }
        log.info("服务器类型:{},context-path:{},port:{},启动耗时：{}毫秒", "tomcat",
                contextPath, serverPort, (System.currentTimeMillis() - start));
        tomcat.getServer().await();
    }

    public static void runUndertow(String[] args) {
        long start = System.currentTimeMillis();
        ClassLoader classLoader = Bootstrap.class.getClassLoader();
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(classLoader)
                .setContextPath(DefaultConfig.contextPath)
                .setResourceManager(new ClassPathResourceManager(classLoader))
                .setDeploymentName("jfinal")
                .setEagerFilterInit(true);
        deploymentInfo.addFilter(
                Servlets.filter("jfinal", JFinalFilter.class)
                        .addInitParam("configClass", DefaultConfig.configClass))
                .addFilterUrlMapping("jfinal", "/*", DispatcherType.REQUEST);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        PathHandler pathHandler = Handlers.path(
                Handlers.resource(new ClassPathResourceManager(classLoader, "webapp")));
        try {
            pathHandler.addPrefixPath(DefaultConfig.contextPath, manager.start());
        } catch (ServletException e) {
            e.printStackTrace();
        }
        Undertow server = Undertow.builder()
                .addHttpListener(DefaultConfig.serverPort, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        log.info("服务器类型:{},context-path:{},port:{},启动耗时：{}毫秒", "undertow",
                DefaultConfig.contextPath, DefaultConfig.serverPort, (System.currentTimeMillis() - start));
    }

}