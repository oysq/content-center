package ribbonconfiguration.impl;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NacosSameClusterWeightRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
    }

    @Override
    public Server choose(Object o) {

        try {
            // 获取目标服务的服务名
            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            String targetName = loadBalancer.getName();

            // 获取服务发现相关的API
            NamingService namingService = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties());

            // 当前集群的名称
            String clusterName = nacosDiscoveryProperties.getClusterName();

            // 健康的目标服务实例
            List<Instance> instanceList = namingService.selectInstances(targetName, true);

            // 过滤出当前集群的实例
            List<Instance> targetInstantList = instanceList.stream().filter(instance -> instance.getClusterName().equals(clusterName)).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(targetInstantList)) {
                log.info("--- 发生跨集群调用 ---");
                targetInstantList = instanceList;
            }

            // 使用 nacos 自带算法
            Instance targetInstance = ExtendBalancer.getHostByRandomWeight2(targetInstantList);
            log.info("NacosSameClusterWeightRule 选择的地址: {}:{}", targetInstance.getIp(), targetInstance.getPort());

            // 返回
            return new NacosServer(targetInstance);
        } catch (NacosException e) {
            log.error("异常", e);
        }
        return null;
    }

}

/**
 * 通过继承的方式，调用别人的 protected 方法
 */
class ExtendBalancer extends Balancer {
    public static Instance getHostByRandomWeight2(List<Instance> hosts) {
        return getHostByRandomWeight(hosts);
    }
}
