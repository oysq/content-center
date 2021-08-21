package com.oysq.contentcenter.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("notice")
public class Notice {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("content")
    private String content;

    @TableField("show_flag")
    private Integer showFlag;

    @TableField("create_time")
    private Date createTime;

}
