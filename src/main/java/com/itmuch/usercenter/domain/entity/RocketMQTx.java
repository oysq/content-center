package com.itmuch.usercenter.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志事务
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RocketMQTx {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("log")
    private String log;

}
