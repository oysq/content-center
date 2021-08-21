package com.oysq.contentcenter.configuration.feign;

import feign.Logger;
import org.springframework.context.annotation.Bean;

// 如果加上下面这个注解，就要放到主类的包外面，防止上下文覆盖导致全局生效的问题
//@Configuration
public class UserCenterFeignConfiguration {

    @Bean
    public Logger.Level level() {
        return Logger.Level.FULL;
    }

}
