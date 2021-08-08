package com.itmuch.usercenter.feign.controller;

import com.itmuch.usercenter.feign.client.BaiduFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("feignTest")
public class FeignTestController {

    @Autowired
    private BaiduFeignClient baiduFeignClient;

    @GetMapping("/test4")
    public String test4() {
        return baiduFeignClient.getBaiduGuoJiNews();
    }

}
