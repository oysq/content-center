package com.oysq.contentcenter.configuration.ribbon;

import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;
import ribbonconfiguration.RibbonConfig;

// 全局配置
@Configuration
@RibbonClients(defaultConfiguration = RibbonConfig.class)
public class GlobalRibbonConfiguration {
}
