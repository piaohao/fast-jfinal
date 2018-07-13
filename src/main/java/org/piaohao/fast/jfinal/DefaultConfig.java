package org.piaohao.fast.jfinal;

import cn.hutool.core.convert.Convert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DefaultConfig {

    public static Integer serverPort;
    public static String contextPath;
    public static String staticPath;
    public static String tomcatBaseDir;
    public static String configClass;

    public static Properties properties = new Properties();

    public static void init(InputStream inputStream) {
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverPort = Convert.toInt(properties.get(Util.SERVER_PORT), 8080);
        contextPath = Convert.toStr(properties.get(Util.CONTEXT_PATH), "");
        staticPath = Convert.toStr(properties.get(Util.STATIC_PATH), "static");
        tomcatBaseDir = Convert.toStr(properties.get(Util.TOMCAT_BASE_DIR), "/tmp/tomcat");
        configClass = Convert.toStr(properties.get(Util.CONFIG_CLASS));
    }
}
