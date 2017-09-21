## pulgin引用
```
<plugin>
    <groupId>com.chuangjiangx.plugin</groupId>
    <artifactId>api-generate</artifactId>
    <version>1.3.3</version>
    <configuration>
        <targetClass>${project.build.sourceDirectory}/com/chuangjiangx/polypay/availability/controller/LklMerchantController.java</targetClass>
        <output>${project.basedir}/api-doc</output>
        <resources>
            <resource>${project.build.sourceDirectory}</resource>
            <resource>${project.parent.basedir}\poly-pay-common\src\main\java</resource>
        </resources>
    </configuration>
</plugin>
```
## 参数配置
1. <targetClass>...</targetClass>  
需要生成文档的java类,只对@Controller和@RestController类起作用。
    a. 生成单个类。${project.build.sourceDirectory}/包名/类名.java
    b. 生成某一包下的多个类。 包名。 例如：com.chuangjiangx.agent.controller.user

2. <output>...</output>
生成文档输出目录

3. <resources>...</resources>
待生成类的源码路径，以及源码依赖包的目录,例如:
<resource>${project.build.sourceDirectory}</resource>

## java类使用
1. 正常添加类、方法、字段注释信息。
2. 对于Response中的data字段为Object类型无法知道运行时类型。
    a. @map 标注 data 类型解析json时的 名称
    b. @see 如果data类型为对象类型，则使用该注解表明类型
例如:
    ```
    /**
    * 模糊查询银行信息
    *
    * @param bankName 搜索条件
    * @return 搜索结果
    * @see LklBankInfoDTO
    */
   @RequestMapping(value = "/search-bank-list", produces = "application/json")
   @Login
   public Response searchBankInfo(@RequestParam String bankName) {
       List<LklBankInfoDTO> list = signLklPolyMerchantApplication.searchBankInfo(bankName);
       return Response.success(list);
   }
    ```
生成结果:

```
+ Response 200 (application/json)

    + Attributes
        + success:`true` (boolean,optional) -
        + errCode:`default` (string,optional) -
        + errMsg:`default` (string,optional) -
        + otherMsg:`default` (string,optional) -
        + data (object, optional) -
            + bankName:`default` (string,optional) -
            + openingBank:`default` (string,optional) -
            + clearingBank:`default` (string,optional) -
```


3. Controller中的接收响应对象字段描述信息。例如
```
    /**
    * 商户姓名
    * @arg 测试01
    */
    @NotEmpty
   private String merchantName;
```
生成结果:

```
+ Request (application/json)

    + Attributes
        + merchantName:`测试01` (string,required) - 商户姓名

```

4. 支持@JsonProperty("")属性转换
```
    /**
    * 商户姓名
    * @arg 测试01
    */
    @JsonProperty("name")
   private String merchantName;
```
生成结果:

```
+ Request (application/json)

    + Attributes
        + name:`测试01` (string,optional) - 商户姓名

```

## 存在问题：
1. 方法注释中不要带英文()号 

2. 响应对象中data对象为array时 该数组中的对象属性描述不会显示
