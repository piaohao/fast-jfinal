package org.piaohao.fast.jfinal;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 简单的工具类,包含一些常亮定义
 *
 * @author piaohao
 */
@Slf4j
public class Util {

    public static final String DEFAULT_PROPERTIES = "fast-jfinal.properties";

    public static final String SERVER_PORT = "server.port";
    public static final String CONTEXT_PATH = "server.context.path";
    public static final String STATIC_PATH = "server.static.path";
    public static final String TOMCAT_BASE_DIR = "tomcat.base.dir";
    public static final String CONFIG_CLASS = "jfinal.config.class";

    public static final String SERVER_TYPE = "server.type";

    @Data
    public static class ProjectType {
        private String path;
        private boolean isJar;
    }

    public enum ServerType {
        TOMCAT("tomcat"), UNDERTOW("undertow");

        private String name;

        ServerType(String name) {
            this.name = name;
        }
    }

    public static ProjectType getProjectType() {
        String resourceURL = ClassUtil.getResourceURL(DEFAULT_PROPERTIES).toString();
        String projectPath = null;
        boolean isJar = false;
        if (resourceURL.startsWith("jar:")) {
            projectPath = StrUtil.subPre(resourceURL, resourceURL.indexOf("!/" + DEFAULT_PROPERTIES));
            projectPath = StrUtil.subSuf(projectPath, "jar:file:".length());
            isJar = true;
        } else {
            projectPath = StrUtil.subPre(resourceURL, resourceURL.indexOf("/" + DEFAULT_PROPERTIES));
            projectPath = StrUtil.subSuf(projectPath, "file:".length());
        }
        ProjectType projectType = new ProjectType();
        projectType.setPath(projectPath);
        projectType.setJar(isJar);
        return projectType;
    }

    public static InputStream getInputStream(String filePath) {
        ProjectType projectType = getProjectType();
        if (projectType == null) {
            return null;
        }
        String path = projectType.getPath();
        if (projectType.isJar()) {
            try {
                URL url = new URL("jar:file:" + path + "!" + filePath);
                return url.openStream();
            } catch (Exception e) {
                log.error("读取jar文件错误!", e);
                return null;
            }
        } else {
            try {
                return new FileInputStream(path + filePath);
            } catch (FileNotFoundException e) {
                log.error("读取文件错误!", e);
                return null;
            }
        }
    }
}
