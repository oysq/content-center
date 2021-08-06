package com.itmuch.usercenter.rocketmq.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("rocketmq_transaction_log")
public class RocketMQTx {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("log")
    private String log;

}
