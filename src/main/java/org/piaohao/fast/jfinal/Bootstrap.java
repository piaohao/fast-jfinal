package org.piaohao.fast.jfinal;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.core.JFinalFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.FixContextListener;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

@Slf4j
public class Bootstrap {

    public static void run(String[] args) {
        long start = System.currentTimeMillis();
        DefaultConfig.init();

        Tomcat tomcat = new Tomcat();
        Integer serverPort = DefaultConfig.serverPort;
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
        log.info("context-path:{},port:{},启动耗时：{}毫秒", contextPath, serverPort, (System.currentTimeMillis() - start));
        tomcat.getServer().await();
    }

}