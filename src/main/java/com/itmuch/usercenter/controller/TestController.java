package com.itmuch.usercenter.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itmuch.usercenter.dao.NoticeMapper;
import com.itmuch.usercenter.domain.entity.Notice;
import com.itmuch.usercenter.feignclient.BaiduFeignClient;
import com.itmuch.usercenter.service.share.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private BaiduFeignClient baiduFeignClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TestService testService;

    @GetMapping("/test")
    public List<Notice> test() {
        Notice notice = Notice.builder()
                .content("这是提示的内容")
                .showFlag(1)
                .createTime(new Date())
                .build();
        noticeMapper.insert(notice);

        QueryWrapper wrapper = new QueryWrapper<Notice>();
        return noticeMapper.selectList(wrapper);
    }

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
        return baiduFeignClient.getBaiduGuoJiNews();
    }

    @GetMapping("/test5")
    public String test5() {
        return "test5";
    }

    @GetMapping("/test6")
    public String test6() throws InterruptedException {
        for(int i = 0 ; i < 20 ; i ++) {
            log.info("test ==> {}", i);
            restTemplate.getForObject("http://content-center:9091/test5", String.class);
            Thread.sleep(500);
        }
        return "模拟结束";
    }

    @GetMapping("/test7")
    public String test7() {
        this.testService.sentinelResource();
        return "test7";
    }

    @GetMapping("/test8")
    public String test8() {
        this.testService.sentinelResource();
        return "test8";
    }

    @GetMapping("/test9")
    public String test9() {
        for(int i = 0 ; i < 20 ; i ++) {
            log.info("test9 ==> {}", i);
            String res = restTemplate.getForObject("http://content-center:9091/test10/"+i, String.class);
            log.info("test9 <== {}", res);
        }
        return "模拟结束";
    }

    @GetMapping("/test10/{id}")
    public String test10(@PathVariable Integer id) throws InterruptedException {
        log.info("test5 rcv {}", id);
        Thread.sleep(2000);
        return "res:"+id;
    }

    @GetMapping("test11")
    @SentinelResource("hot")
    public String test11(@RequestParam(required = false) String a,
                         @RequestParam(required = false) String b) {
        return a+" "+b;
    }

}
