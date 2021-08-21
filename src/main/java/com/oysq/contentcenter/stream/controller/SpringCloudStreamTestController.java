package com.oysq.contentcenter.stream.controller;

import com.oysq.contentcenter.stream.output.MySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("SpringCloudStreamTest")
public class SpringCloudStreamTestController {

    // 这里的 Source 对应的是启动类上的注解
    @Autowired
    private Source source;

    @Autowired
    private MySource mySource;

    @GetMapping("/send")
    public String send() {

        // 发送消息
        this.source.output().send(
                MessageBuilder.withPayload("这是一条消息体").setHeader("my-header", "test-123").build()
        );

        return "success";
    }

    @GetMapping("/sendMy")
    public String sendMy() {

        // 发送消息
        this.mySource.output().send(
                MessageBuilder.withPayload("这是一条MySource的消息体").build()
        );

        return "success";
    }

}
