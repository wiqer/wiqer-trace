# wiqer-trace
全链路id 支持web 服务和兼容dubbo 阿里和apache版本

根据自己的项目dubbo使用版本，来排除apache或者alibaba的dubbo的pom依赖

## 1 web 服务 接入

pom坐标

```
<dependency>
    <groupId>io.github.wiqer</groupId>
    <artifactId>wiqer-trace</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <artifactId>dubbo</artifactId>
            <groupId>org.apache.dubbo</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

启动类加上注解
```
@Import({WebLogConfig.class, WebLogInterceptor.class})
```
## 2 service接入
pom坐标
```
<dependency>
    <groupId>io.github.wiqer</groupId>
    <artifactId>wiqer-trace</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <artifactId>dubbo</artifactId>
            <groupId>com.alibaba</groupId>
        </exclusion>
    </exclusions>
</dependency>
```
## 3 前端接入
前端每次请求携带用户id和traceid

WIQER_TRACE ：一般为uuid

WIQER_USER：登陆后获取的用户id即可
