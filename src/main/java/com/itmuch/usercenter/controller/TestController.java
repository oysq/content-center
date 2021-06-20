package com.itmuch.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itmuch.usercenter.dao.NoticeMapper;
import com.itmuch.usercenter.domain.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private DiscoveryClient discoveryClient;

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


}
