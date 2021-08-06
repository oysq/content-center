package com.itmuch.usercenter.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddBonusMessage {

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 加多少积分
     */
    private Integer bonus;

}
