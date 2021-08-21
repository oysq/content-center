package com.oysq.contentcenter.share.domain.dto;

import com.oysq.contentcenter.share.domain.enums.AuditStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareAuditDTO {

    /**
     * 审核状态
     */
    private AuditStatusEnum auditStatus;

    /**
     * 原因
     */
    private String reason;

}
