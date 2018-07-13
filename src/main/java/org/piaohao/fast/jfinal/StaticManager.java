package org.piaohao.fast.jfinal;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class StaticManager {

    @Getter
    private static Map<String, StaticInfo> staticFileMap = new HashMap<>();

    public static void put(String path, StaticInfo entry) {
        staticFileMap.put(path, entry);
    }

    public static StaticInfo get(String path) {
        return staticFileMap.get(path);
    }

    public static void init() throws IOException {
        Util.ProjectType projectType = Util.getProjectType();
        if (projectType == null) {
            return;
        }
        String path = projectType.getPath();
        if (projectType.isJar()) {
            JarFile jarFile = new JarFile(path);
            JarEntry defaultPropertiesEntry = jarFile.stream()
                    .filter(jar -> jar.getName().equals(Util.DEFAULT_PROPERTIES))
                    .findFirst()
                    .get();
            log.info("配置文件路径为:{}", defaultPropertiesEntry.getName());
            DefaultConfig.init(jarFile.getInputStream(defaultPropertiesEntry));
            jarFile.stream()
                    .filter(jar -> jar.getName().startsWith(DefaultConfig.staticPath + "/") && !jar.isDirectory())
                    .forEach(jar -> {
                        String name = jar.getName();
                        name = StrUtil.addPrefixIfNot(StrUtil.replace(name, "\\", "/"), "/");
                        StaticManager.put(name,
                                StaticManager.StaticInfo
                                        .builder()
                                        .path(name)
                                        .size(jar.getSize())
                                        .lastModifyTime(jar.getLastModifiedTime().toMillis())
                                        .build());
                    });
        } else {
            DefaultConfig.init(FileUtil.getInputStream(path + "/" + Util.DEFAULT_PROPERTIES));
            String templatePath = ClassUtil.getClassPath() + "/" + DefaultConfig.staticPath;
            FileUtil.loopFiles(templatePath)
                    .forEach(f -> {
                        String absolutePath = f.getAbsolutePath();
                        absolutePath = absolutePath.substring(ClassUtil.getClassPath().length());
                        absolutePath = StrUtil.addPrefixIfNot(StrUtil.replace(absolutePath, "\\", "/"), "/");
                        StaticManager.put(absolutePath,
                                StaticManager.StaticInfo
                                        .builder()
                                        .path(absolutePath)
                                        .size(f.getTotalSpace())
                                        .lastModifyTime(f.lastModified())
                                        .build());
                    });
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaticInfo {
        private String path;
        private Long size;
        private Long lastModifyTime;
    }
}
