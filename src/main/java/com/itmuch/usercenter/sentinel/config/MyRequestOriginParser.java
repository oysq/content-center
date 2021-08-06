package com.itmuch.usercenter.sentinel.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

//@Component //暂时注释掉，不让生效
public class MyRequestOriginParser implements RequestOriginParser {
    @Override
    public String parseOrigin(HttpServletRequest httpServletRequest) {

        String origin = httpServletRequest.getParameter("origin");
        if(StringUtils.isBlank(origin)) {
            throw new IllegalArgumentException("origin 不可空");
        }
        // 设置本次请求的来源
        return origin;
    }
}
