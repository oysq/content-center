package com.itmuch.usercenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign 脱离 Ribbon 使用
 */
@FeignClient(name = "baidu", url = "http://news.baidu.com")
public interface BaiduFeignClient {

    @GetMapping("/guoji")
    String getBaiduGuoJiNews();

}
