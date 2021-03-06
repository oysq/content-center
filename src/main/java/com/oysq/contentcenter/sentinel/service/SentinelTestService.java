package com.oysq.contentcenter.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SentinelTestService {

    @SentinelResource("testSentinelResource")
    public String sentinelResource() {
        log.info("这是一个被 Sentinel 标记为资源的入口");
        return "test";
    }

}
