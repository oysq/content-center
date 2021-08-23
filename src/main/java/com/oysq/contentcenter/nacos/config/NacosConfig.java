package com.oysq.contentcenter.nacos.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Data
@Component
public class NacosConfig {

    @Value("${my.config.val}")
    private String myConfig;

}
