package com.itmuch.usercenter;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import com.itmuch.usercenter.configuration.feign.GlobalFeignConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@MapperScan("com.itmuch.usercenter.dao")
@SpringBootApplication
@EnableFeignClients(defaultConfiguration = GlobalFeignConfiguration.class)
public class ContentCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentCenterApplication.class, args);
    }

    @Bean
    @LoadBalanced
    @SentinelRestTemplate
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
