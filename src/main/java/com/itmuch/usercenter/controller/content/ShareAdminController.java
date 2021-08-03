package com.itmuch.usercenter.controller.content;

import com.itmuch.usercenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.usercenter.domain.entity.Share;
import com.itmuch.usercenter.service.share.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
public class ShareAdminController {

    @Autowired
    private ShareService shareService;

    @PutMapping("/audit/{shareId}")
    public Share auditById(@PathVariable String shareId, @RequestBody ShareAuditDTO shareAuditDTO) {

        // TODO 认证，授权

        // 处理业务
        return shareService.auditById(shareId, shareAuditDTO);
    }

}
