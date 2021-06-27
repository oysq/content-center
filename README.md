# Spring-cloud-Alibaba 实现

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
      1. 配置方式指定`Apache`的`httpclient`，支持连接池，性能提高15%
4. 继承
   1. 分析优缺点（个人认为：违背了微服务松耦合的最初目的）
5. 此外还支持编码器、解码器、通用逻辑等
   
---

### Sentinel

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
   
#### 流控规则

> 针对某个资源（微服务的端点或标记为 @SentinelResource 资源的入口）进行流量限制，达到阀值请求拒绝

1. 针对来源：可以指定某个微服务（default：所有）

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
       3. 当B达到阀值时，B的请求将被拒绝，而其他链路上的请求（如C）不受影响
     * 场景：相当于更细粒度的流控规则，可以细化到某个 `@service`，此时该 `@service` 相当于一个抽象的微服务，而B和C则是调用方，该规则可以用来限制来源。

4. 流控效果

> 为什么要拒绝请求？简言之，至少用户多刷新几次还是会有成功请求的，要是没控制流量导致微服务崩溃，用户就怎么刷新都不会成功了。

* 快速失效
  * 规则：达到阀值直接拒绝请求，保护微服务不会崩溃。
* Warm Up
  * 规则：指定单机阀值（QPS/线程数）为X，预热时长为T秒，则实际的实时阀值是从 X/coldFactor 开始逐渐增加（冷却因子 coldFactor 默认是3），经过 T 秒才会到达 X。每个时刻的阀值都是不一样的，某时刻到达该时刻的阀值时，该时刻多余的请求都会被拒绝。
  * 场景：适用于平时流量都不高（不会申请太多的内存、线程等），但偶尔会有秒杀类大流量的并发场景。微服务需要随着流量的每一次提高而花费准备时间（加载对应缓存、申请更多线程等），如果没有给到预热时间，会直接把微服务搞崩。
* 排队等待
  * 规则：指定单机阀值QPS为X，超时时间为T秒，则当流量超过阀值X时，不是抛弃，而是把多于的请求放入队列进行排队。当一个请求的排队等待时间超过T秒时，将该请求拒绝。
  * 场景：消息队列等对实时性要求不高的场景，起到 ”削峰填谷“ 的作用。
  * 注意：排队等待的规则，阀值类型只能选 QPS，不支持选线程数。









