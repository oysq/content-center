package com.oysq.contentcenter.share.controller;

import com.oysq.contentcenter.share.domain.dto.ShareAuditDTO;
import com.oysq.contentcenter.share.domain.entity.Share;
import com.oysq.contentcenter.share.service.ShareAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("/audit/test")
    public Share auditTest(@RequestBody ShareAuditDTO shareAuditDTO) {

        return Share.builder().id(UUID.randomUUID().toString()).author("oysq").build();
    }



}
