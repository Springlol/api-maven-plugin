# API文档生成的maven插件
对SpringMVC框架接口生成Blueprint格式的md文档

支持框架：SpringMVC

**在pom.xml中增加**
```
<plugin>
        <groupId>com.chuangjiangx.plugin</groupId>
        <artifactId>api-generate</artifactId>
        <version>1.3.0</version>
        <configuration>
            <baseClass>
                com.chuangjiangx.agent.controller.user
                <!--${project.build.sourceDirectory}/com/chuangjiangx/agent/controller/user-->
            </baseClass>
            <output>${project.basedir}/apid</output>
            <resources>
                <resource>${project.build.sourceDirectory}</resource>
            </resources>
        </configuration>
    </plugin>
```
# 配置参数说明
- baseClass 指定生成的包或者类，支持指定类或者包操作。例如
  1. com.chuangjiangx.agent.controller.user 指定需要生成的包名
  2. ${project.build.sourceDirectory}/com/chuangjiangx/agent/controller/user/UserController.java  指定要生成的java

- output 指定生成的md文件的存放目录，需要手动创建文件夹
    例如：${project.basedir}/apid
- resources  指定扫描包或者类的源文件地址
    例如：<resource>${project.build.sourceDirectory}</resource>



# JAVA代码注释规则

- 方法注释
    1. /** ... */  接口描述
    2. @param paramName  设置参数描述
    3. @see qulifiedTypeName 设置响应response中data字段类型。
    4. @map string 如果响应response中的data字段类型为List，使用该注解。

```
    //data为单个对象
    @see User
    {
        "isSuccess":true,
        "err_code":"1000",
        "err_msg":"错误描述",
        "data":
            {
                "id":1,
                "name":"张三"
            }
    }
    
    //data为List<User>集合
    /**
    * @map users
    * @see User
    */
    {
        "isSuccess":true,
            "err_code":"1000",
            "err_msg":"错误描述",
            "users": [
                {
                    "id":1,
                    "name":""名称
                }
            ]
    }
```

- 字段注释规则
    1. /** ... */ 字段描述
    2. @arg 生成时字段值举例
    
```
    /**
    * 姓名
    * @arg zhangsan
    */
    private String name;
    
    {
        ...
        "name":"zhangsan"
    }
```

