package com.itmuch.usercenter.nacos.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController("nacosTest")
public class NacosTestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/test2")
    public List<ServiceInstance> test2() {
        return discoveryClient.getInstances("user-center");
    }

    @GetMapping("/test3")
    public List<String> test3() {
        return discoveryClient.getServices();
    }

}
