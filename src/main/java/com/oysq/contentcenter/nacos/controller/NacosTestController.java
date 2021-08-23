package com.oysq.contentcenter.nacos.controller;

import com.oysq.contentcenter.nacos.config.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("nacosTest")
public class NacosTestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private NacosConfig nacosConfig;

    @GetMapping("/test2")
    public List<ServiceInstance> test2() {
        return discoveryClient.getInstances("user-center");
    }

    @GetMapping("/test3")
    public List<String> test3() {
        return discoveryClient.getServices();
    }

    @GetMapping("/test4")
    public String test4() {
        return nacosConfig.getMyConfig();
    }



}
