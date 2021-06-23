package com.itmuch.usercenter.feignclient;

import com.itmuch.usercenter.configuration.feign.UserCenterFeignConfiguration;
import com.itmuch.usercenter.domain.dto.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// 局部配置 Feign 的位置
//@FeignClient(name = "user-center", configuration = UserCenterFeignConfiguration.class)
@FeignClient(name = "user-center")
public interface UserCenterFeignClient {

    /**
     * http://user-center/user/{id}
     * @param id
     * @return
     */
    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable(value = "id") Integer id);

}
