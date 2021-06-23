# Spring-cloud-Alibaba 实现

## 核心功能

1. 基础框架
   1. 框架选型
      1. `SpringBoot`
      2. `SpringCloud`
      3. `SpringCloud-Alibaba`
   2. 版本关系 [链接](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

2. Nacos
   1. Nacos中文手册 [链接](https://nacos.io/zh-cn/docs/what-is-nacos.html)
   2. Nacos 领域模型
      1. namespace
      2. group
      3. service
      4. cluster
      6. instance
   3. 元数据
   4. 服务注册
   5. 服务发现

3. Ribbon
   1. 手动实现客户端`RestTemplate`负载均衡
   2. 通过`Ribbon`实现`RestTemplate`客户端调用负载均衡
      1. 设置/重写 Ribbon 暴露的配置接口
         1. 实现方式
            1. 配置文件（优先级更高）
            2. 代码实现（用于细粒度场景、自定义实现）
         2. 影响范围
            1. 全局生效
            2. 指定服务配置（注意父子上下文覆盖问题）
         3. 修改`IRule`接口默认的`ZoneAvoidanceRule`实现
            1. 根据权重均衡
            2. 优先使用同 cluster 内的 instance
            3. 匹配元数据相同版本