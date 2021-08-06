package com.itmuch.usercenter.share.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Share {

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField("user_id")
    private Integer userId;

    @TableField("title")
    private String title;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否原创 0:否 1:是
     */
    @TableField("is_original")
    private Boolean isOriginal;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 封面
     */
    @TableField("cover")
    private String cover;

    /**
     * 概要信息
     */
    @TableField("summary")
    private String summary;

    /**
     * 价格（需要的积分）
     */
    @TableField("price")
    private Integer price;

    /**
     * 下载地址
     */
    @TableField("download_url")
    private String downloadUrl;

    /**
     * 下载数
     */
    @TableField("buy_count")
    private Integer buyCount;

    /**
     * 是否显示 0:否 1:是
     */
    @TableField("show_flag")
    private Boolean showFlag;

    /**
     * 审核状态 NOT_YET: 待审核 PASSED:审核通过 REJECTED:审核不通过
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 审核不通过原因
     */
    @TableField("reason")
    private String reason;

}
