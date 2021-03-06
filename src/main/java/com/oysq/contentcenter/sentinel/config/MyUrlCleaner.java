package com.oysq.contentcenter.sentinel.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class MyUrlCleaner implements UrlCleaner {
    @Override
    public String clean(String s) {

        // 拦截处理后返回链路资源名称
        // 实现对 RESTful URL的预处理： /share/1 和 /share/2 的资源名都是 /share/{id}

        // 不生效原因：该版本的 sentinel 传进来的是 “/share/{id}” 字符串

        return Arrays.stream(s.split("/"))
                .map(str -> {
                    if(NumberUtils.isNumber(str)) {
                        return "{userId}";
                    }
                    return str;
                })
                .reduce((a, b) -> a + "/" + b)
                .orElse("");
    }
}
