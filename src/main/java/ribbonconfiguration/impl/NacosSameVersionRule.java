package ribbonconfiguration.impl;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NacosSameVersionRule extends AbstractLoadBalancerRule {

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

            // 当前实例的版本
            String myVersion = nacosDiscoveryProperties.getMetadata().get("version");

            // 健康的目标服务实例
            List<Instance> instanceList = namingService.selectInstances(targetName, true);

            // 过滤出相同版本的目标实例
            List<Instance> targetInstanceList = instanceList.stream().filter(instance -> instance.getMetadata().get("version").equals(myVersion)).collect(Collectors.toList());

            // 随机算法
            Instance targetInstance = targetInstanceList.get(RandomUtils.nextInt(targetInstanceList.size()));
            log.info("NacosSameVersionRule 选择的地址: {}:{}", targetInstance.getIp(), targetInstance.getPort());

            // 返回
            return new NacosServer(targetInstance);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }
}

