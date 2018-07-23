# fast-jfinal
jfinal项目以embed-tomcat或undertow的方式运行,无须打war包,无续部署tomcat

用appassembler-maven-plugin打包，然后运行bin目录中的脚本即可。

具体参见demo项目:[fast-jfinal-demo](https://github.com/piaohao/fast-jfinal-demo)

### 1.pom.xml
引入
````
<dependency>
    <groupId>org.piaohao</groupId>
    <artifactId>fast-jfinal</artifactId>
    <version>1.0.3</version>
</dependency>
````

加入appassembler打包插件
````
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>appassembler-maven-plugin</artifactId>
    <version>1.10</version>
    <configuration>
        <!-- 生成linux, windows两种平台的执行脚本 -->
        <platforms>
            <platform>windows</platform>
            <platform>unix</platform>
        </platforms>
        <!-- 根目录 -->
        <assembleDirectory>${project.build.directory}/${project.name}</assembleDirectory>
        <!-- 打包的jar，以及maven依赖的jar放到这个目录里面 -->
        <repositoryName>lib</repositoryName>
        <!-- 可执行脚本的目录 -->
        <binFolder>bin</binFolder>
        <!-- 配置文件的目标目录 -->
        <configurationDirectory>webapp</configurationDirectory>
        <!-- 拷贝配置文件到上面的目录中 -->
        <copyConfigurationDirectory>true</copyConfigurationDirectory>
        <!-- 从哪里拷贝配置文件 (默认src/main/config) -->
        <configurationSourceDirectory>src/main/resources</configurationSourceDirectory>
        <!-- lib目录中jar的存放规则，默认是${groupId}/${artifactId}的目录格式，flat表示直接把jar放到lib目录 -->
        <repositoryLayout>flat</repositoryLayout>
        <encoding>UTF-8</encoding>
        <logsDirectory>logs</logsDirectory>
        <tempDirectory>tmp</tempDirectory>
        <programs>
            <program>
                <id>${project.name}-${project.version}</id>
                <!-- 启动类 -->
                <mainClass>org.piaohao.fast.jfinal.Bootstrap</mainClass>
                <jvmSettings>
                    <extraArguments>
                        <extraArgument>-server</extraArgument>
                        <extraArgument>-Xmx200M</extraArgument>
                        <extraArgument>-Xms200M</extraArgument>
                    </extraArguments>
                </jvmSettings>
            </program>
        </programs>
    </configuration>
</plugin>
````

### 2.fast-jfinal.properties
````
server.port=8080  #启动端口
server.type=tomcat #或者undertow,不指定的情况下,以undertow运行
server.context.path=/    #项目上下文路径
tomcat.base.dir=/tmp/tomcat    #tomcat临时文件目录，可不设置
jfinal.config.class=org.piaohao.fast.jfinal.demo.DemoConfig    #JfinalConfig配置类
````

### 3.JFinalConfig
````
PathKit.setWebRootPath("WEB-INF/view");  //设置web视图根路径，放在resources目录下
````

````
engine.setSourceFactory(new ClassPathSourceFactory());  //设置jfinal模板引擎的工厂
````
