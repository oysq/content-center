# Spring-cloud-Alibaba 实现
________

## 核心功能

### 基础框架

> 版本关系 [链接](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

#### 核心
* `SpringBoot`
* `SpringCloud`
* `SpringCloud-Alibaba`

#### 微服务组件
* `Nacos`
* `Ribbon`
* `Feign`
* `Sentinel`

### 其他插件
* `mybatis-plus`
* `Lombok`

---

### Nacos

> Nacos中文手册 [链接](https://nacos.io/zh-cn/docs/what-is-nacos.html)
   
1. Nacos 领域模型
   1. namespace
   2. group
   3. service
   4. cluster
   6. instance
3. 元数据
4. 服务注册
5. 服务发现

---

### Ribbon

1. 手动实现客户端`RestTemplate`负载均衡
2. 通过 Ribbon 实现`RestTemplate`客户端调用负载均衡（`@LoadBalanced`）
   1. 设置/重写 Ribbon 暴露的配置接口
      1. 实现方式
         1. 配置文件方式（优先级更高，无需重启）
         2. 代码实现方式（用于细粒度场景、自定义实现）
      2. 影响范围
         1. 全局生效
         2. 指定服务配置（注意父子上下文覆盖问题）
      3. 修改`IRule`接口默认的`ZoneAvoidanceRule`实现
         1. 根据权重均衡
         2. 优先使用同 cluster 内的 instance
         3. 匹配元数据相同版本

---

### Feign

1. 通过 Feign 实现远程调用
   1. 实现方式：Contract 契约（`@FeignClient`注解支持`SpringMVC`的注解）
   2. 与 Ribbon 配合
      1. Feign 默认会结合 Ribbon 的实现负载均衡
      2. 脱离 Ribbon 的方式，直接指定实际 URL 地址
2. 修改 Feign 的日志级别（默认不打印任何日志，打印需要在 logging 本身配置是debug 级别下才生效）
   1. 配置文件方式（优先级更高，无需重启）
   2. 代码实现方式（用于细粒度场景、自定义实现）
3. 修改 Feign 最终使用的 Http 客户端
   1. 默认情况
      1. 不和 Ribbon 配合时，使用`HttpUrlConnection`，不支持连接池，性能差
      2. 和 Ribbon 结合时，支持自定义客户端，默认是`HttpUrlConnection`
   2. 优化
      1. yml方式配置指定`Apache`的`httpclient`，支持连接池，性能提高15%
4. 继承
   1. 分析优缺点（个人认为：违背了微服务松耦合的最初目的）
5. 此外还支持编码器、解码器、通用逻辑等
   
---

### Sentinel

> Sentinel中文手册 [链接](https://sentinelguard.io/zh-cn/)

#### 雪崩效应

> 现象：A服务停服，导致B服务线程池所有线程超时，B服务也停服，以此类推。
> 
> 别名：级联失效、级联效应、cascading failure

解决方式:

* 超时：缩短B服务请求超时时间，加快B服务线程释放速度
* 限流：限制B服务的流量，超过限制直接拒绝，留一部分线程提供其它支持
* 仓壁模式：B服务每个模块维护自己的线程池，互相隔离（鸡蛋不放在一个篮子里）
* 断路器模式：
   1. B服务设置一个规则，踩线就打开断路器，不在请求A服务，对外直接返回失败
   2. 经过一段时间，断路器自动进入半开状态，对A服务发起一次试探请求，若成功，则关掉断路器，否则继续打开断路器，等待下一次试探。

#### 规则介绍

##### 流控规则

> 针对某个资源（微服务的端点或标记为 @SentinelResource 资源的入口）进行流量限制，达到阀值请求拒绝

1. 针对来源：可以指定该规则只针对来自某个微服务的请求（default：所有）

2. 阀值类型
   * QPS（单机阀值：每秒请求数量）
   * 线程数

3. 流控模式
   * 直接：直接限制该资源的流量
   * 关联
      * 规则：对资源A设置流控规则，指定关联资源B，当B达到流控阀值时，该资源A拒绝请求。
      * 场景：比如同一张表的编辑和查询接口，可以相互制约。
   * 链路：
      * 规则：
         1. 存在两条请求链路，B->A，C->A
         2. 对资源A设置流控规则，指定入口资源B
         3. 当来自B的请求达到阀值时，B的请求将被拒绝，而其他链路上的请求（如C）不受影响
      * 场景：相当于更细粒度的流控规则，可以细化到某个 `@service`，此时该 `@service` 相当于一个抽象的微服务，而B和C则是调用方，该规则可以用来限制来源。

4. 流控效果

> 为什么要拒绝请求？简言之，至少用户多刷新几次还是会有成功请求的，要是没控制流量导致微服务崩溃，用户就怎么刷新都不会成功了。

* 快速失效
  * 规则：达到阀值直接拒绝请求，保护微服务不会崩溃。
* Warm Up
  * 规则：指定单机阀值（QPS/线程数）为X，预热时长为T秒，则实际的实时阀值是从 X/coldFactor 开始逐渐增加（冷加载因子 coldFactor 默认是3），经过 T 秒才会到达 X。每个时刻的阀值都是不一样的，某时刻到达该时刻的阀值时，该时刻多余的请求都会被拒绝。
  * 场景：适用于平时流量都不高（不会申请太多的内存、线程等），但偶尔会有秒杀类大流量的并发场景。微服务需要随着流量的每一次提高而花费准备时间（加载对应缓存、申请更多线程等），如果没有给到预热时间，会直接把微服务搞崩。
* 排队等待
  * 规则：指定单机阀值QPS为X，超时时间为T秒，则当流量超过阀值X时，不是抛弃，而是把多于的请求放入队列进行排队。当一个请求的排队等待时间超过T秒时，将该请求拒绝。
  * 场景：消息队列等对实时性要求不高的场景，起到 ”削峰填谷“ 的作用。
  * 注意：排队等待的规则，阀值类型只能选 QPS，不支持选线程数。

##### 服务熔断降级

> 服务降级即断路器模式，异常请求达到一定规则就熔断该资源。

1. 熔断策略
   1. 慢调用比例
      * 规则：指定最大RT（即最大响应时间），超过该响应时间的请求计为慢调用。当单位时间（1秒）内请求数量达到最小请求数，且慢调用数量达到指定比例时，触发断路器。经过指定的熔断时长后，熔断器会进入探测恢复状态（HALF-OPEN 状态），若接下来的一个请求响应时间小于设置的慢调用 RT 则结束熔断，若大于设置的慢调用 RT 则会再次被熔断。
      * 注意：RT的最大值为4900ms，超过该值需要指定java的启动参数。
   2. 异常比例
      * 规则：当单位时间（1秒）内请求数量达到最小请求数，且异常数量达到指定比例时，触发断路器。后续同上。
   3. 异常数
      * 规则：当单位时间（1分钟）内请求数量达到最小请求数，且异常数量达到指定数量时，触发断路器。后续同上。
      * 注意：这里的单位时间是1分钟。
    
##### 热点参数限流

> 热点参数可以针对服务的参数进行限流

1. 策略
   * 当在指定的时间窗口(单位：秒)内，指定的第N个参数（从0开始）相同值的请求达到指定阀值（不是QPS，是次数），该参数的该值将被限流，该参数的其他值，或该接口的其他参数不受影响。
   * 配置完某个参数后，还可以为该参数的某几个值配置自己的阀值。

2.注意：参数必须只能是基本类型（int，long，double..等）或者String

##### 系统规则

> 系统规则是对整个应用的流量进行限流

1. 阀值类型
   * LOAD（仅对 Linux/Unix-like 机器生效）：当系统 load1 超过阈值，且系统当前的并发线程数超过系统容量时才会触发系统保护。系统容量由系统的 maxQps * minRt 计算得出。设定参考值一般是 CPU cores * 2.5
   * CPU usage（1.5.0+ 版本）：当系统 CPU 使用率超过阈值即触发系统保护（取值范围 0.0-1.0）。
   * RT：当单台机器上所有入口流量的平均 RT 达到阈值即触发系统保护，单位是毫秒。
   * 线程数：当单台机器上所有入口流量的并发线程数达到阈值即触发系统保护。
   * 入口 QPS：当单台机器上所有入口流量的 QPS 达到阈值即触发系统保护。

##### 授权规则

> 授权规则可以针对来源的微服务设置黑白名单

1. 类型
   * 白名单
   * 黑名单

#### 代码方式配置

> 查 Sentinel 手册 [链接](https://github.com/alibaba/Sentinel/wiki/%E4%BB%8B%E7%BB%8D)

#### 应用与 Sentinel 控制台通信的方式

> 应用集成 sentinel-transport-simple-http.jar 并配置了控制台地址之后，会主动将自己注册到控制台，并发送心跳。且还会开放一个IP+端口用来和控制台通信，IP和端口号可以在应用的 /actuator/sentinel 端点或 sentinel 控制台上查看。例如：http://192.168.1.6:8720/api 可以查看暴露出来的所有端点。

注意：
  * 控制台地址、心跳时间、应用开放的 ip + port 都是可以在应用的 yml 的配置的。
  * 控制台地址必须配置。
  * 应用 ip 没配置，会自动生成一个。
  * 应用 port 没配置，会从8719开始扫描，依次加1，直到找到未被占用的端口。
  * 心跳时间：默认10秒

#### Sentinel 控制台参数配置

> Sentinel 支持在启动控制台时指定 java 启动参数来修改地址、端口、账号密码等信息。

#### Sentinel 核心 API

> 不管是通过代码还是其它优雅的方式，底层的核心都是这三个API

1. SphU：定义资源
2. Tracer：统计异常
3. ContextUtil：标记来源

#### Sentinel 注解埋点支持

> 通过 `@SentinelResource` 注解的方式处理限流和降级，注意部分功能是 `1.6.0` 版本开始才支持

`@SentinelResource` 注解包含以下属性：

1. `value`：资源名称（注解的方式暂不支持指定来源）
2. `entryType`；entry 类型，可选项（默认为 EntryType.OUT）
3. `blockHandler` / `blockHandlerClass`：指定 `BlockException` 的处理方法和所在类。处理方法需要满足一下条件：
   * 使用`public` 修饰符
   * 返回值类型与原方法相同
   * 参数类型需要和原方法相匹配并且最后加一个额外的参数，类型为 `BlockException`
   * 若有指定 `blockHandlerClass`，则处理类必须使用 `static` 修饰符
4. `fallback` / `fallbackClass`：指定异常时的处理函数（只要有异常就会进入该函数，而不是降级才会进入，所有异常都会进入，除非被 `exceptionsToIgnore` 排除。）处理方法需要满足一下条件：
   * 使用`public` 修饰符
   * 方法参数列表需要和原函数一致，或者可以额外多一个 Throwable 类型的参数用于接收对应的异常
   * 若有指定 `blockHandlerClass`，则处理类必须使用 `static` 修饰符
5. `defaultFallback`（since 1.6.0）：默认的 `fallback` 函数名称，可选项，通常用于通用的 `fallback` 逻辑
6. `exceptionsToIgnore`（since 1.6.0）：用于指定哪些异常被排除掉，不会计入异常统计中，也不会进入 fallback 逻辑中，而是会原样抛出。

#### RestTemplate 整合 Sentinel

> 通过 `@SentinelRestTemplate` 注解即可，类似的，当 `RestTemplate` 发起的调用过多时限流、异常时降级。

注意：可以通过在yml的 resttemplate.sentinel.enabled 配置进行启用或关闭

#### Feign 整合 Sentinel

> 通过在yml的 feign.sentinel.enabled = true 配置即可

注意：可以在 `@FeignClient` 注解里指定限流和降级的处理方法。

#### Sentinel 数据持久化

> Sentinel 本身的数据只要重启就会丢失，需要自己集成第三方配置中心或者存储，添加持久化功能。（或者不再自己搭建控制台，使用阿里云提供的 `AHAS` 在线托管服务）

#### 集群流控

> 集群流控目前不够完善，无法用于生产环境，且功能效果可以被网关替代。

使用方式：集成 `TokenServer`，包括独立部署一个 `TokenServer`，或将 `TokenServer` 嵌入到业务的微服务两种方式。

#### 区分来源

> 除了可以通过 `ContextUtil` 标记来源之外，还可以通过实现 `RequestOriginParser` 接口来做全局来源标记。

#### RESTFul Url 路径整合

> 可以通过实现 `UrlCleaner` 接口来做全局资源路径处理，例如将 /share/{id} 和 /share/{age} 这两个请求的资源名称都改为 /share/select，这样就可以为两个资源配置同一个限流降级规则

---

### RocketMQ

> RocketMQ 中文手册 [链接](https://github.com/apache/rocketmq/tree/master/docs/cn)

#### 同类产品比较

> 对比 `Kafka`、`RocketMQ`、`RabbitMQ` [链接](https://www.imooc.com/article/290040)

#### windows 免安装部署

> 服务端分为 NameServer 和 Broker 两个服务。可以另外单独部署可视化控制台界面。

1. 下载包
   * RocketMQ [链接](http://rocketmq.apache.org/release_notes/release-notes-4.2.0/)
   * 控制台界面 [链接](https://github.com/apache/rocketmq-externals)
2. 启动 RocketMQ
   1. 配置环境变量 `ROCKETMQ_HOME` 为解压路径后 `bin` 文件夹所在路径，如：D:\tool\RocketMQ\rocketmq-all-4.2.0-bin-release
   2. `cmd` 进入 `bin` 文件夹，执行命令 `start mqnamesrv.cmd` 启动 **nameServer**
   3. `cmd` 进入 `bin` 文件夹，执行命令 `start mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true` 启动 **broker**
      * 这里的 -n 参数指定的是 nameServer 的地址端口
      * 弹窗界面没有任何提示是正常的
3. 启动控制台界面
   1. 控制台为 SpringBoot 项目，解压后进入 `rocketmq-externals\rocketmq-console\src\main\resources` 文件夹，打开 `application.properties` 进行配置
      * `server.port` = 控制台端口
      * `rocketmq.config.namesrvAddr` = nameServer地址端口，也可以启动控制台后在页面上配置。
   2. 进入 `\rocketmq-externals\rocketmq-console` 文件夹，执行 `mvn clean package -Dmaven.test.skip=true`，编译生成 target 文件
   3. `cmd` 进入 target 文件夹，执行 `java -jar rocketmq-console-ng-1.0.0.jar`，启动控制台
   4. 浏览器访问配置的端口地址
    
