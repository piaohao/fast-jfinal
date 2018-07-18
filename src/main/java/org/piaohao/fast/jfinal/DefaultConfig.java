package org.piaohao.fast.jfinal;

import com.jfinal.kit.PropKit;

public class DefaultConfig {

    public static Integer serverPort;
    public static String contextPath;
    public static String staticPath;
    public static String tomcatBaseDir;
    public static String configClass;

    public static void init() {
        PropKit.use(Util.DEFAULT_PROPERTIES);
        serverPort = PropKit.getInt(Util.SERVER_PORT, 8080);
        contextPath = PropKit.get(Util.CONTEXT_PATH, "");
        staticPath = PropKit.get(Util.STATIC_PATH, "static");
        tomcatBaseDir = PropKit.get(Util.TOMCAT_BASE_DIR, "/tmp/tomcat");
        configClass = PropKit.get(Util.CONFIG_CLASS);
        PropKit.useless(Util.DEFAULT_PROPERTIES);
    }

}
