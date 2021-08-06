package com.itmuch.usercenter.share.controller;

import com.itmuch.usercenter.share.domain.dto.ShareAuditDTO;
import com.itmuch.usercenter.share.domain.entity.Share;
import com.itmuch.usercenter.share.service.ShareAdminService;
import com.itmuch.usercenter.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
public class ShareAdminController {

    @Autowired
    private ShareAdminService shareAdminService;

    @PutMapping("/audit/{shareId}")
    public Share auditById(@PathVariable String shareId, @RequestBody ShareAuditDTO shareAuditDTO) {

        // TODO 认证，授权

        // 处理业务
        return shareAdminService.auditById(shareId, shareAuditDTO);
    }

}
