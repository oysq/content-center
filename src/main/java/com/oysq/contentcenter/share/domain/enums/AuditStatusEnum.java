package com.oysq.contentcenter.share.domain.enums;

import lombok.Getter;

/**
 * 审核状态
 */
@Getter
public enum AuditStatusEnum {

    /**
     * 审核中
     */
    NOT_YET,
    /**
     * 通过
     */
    PASS,
    /**
     * 拒绝
     */
    REJECT

}
