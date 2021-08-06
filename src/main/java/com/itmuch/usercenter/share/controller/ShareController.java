package com.itmuch.usercenter.share.controller;

import com.itmuch.usercenter.share.domain.dto.ShareDTO;
import com.itmuch.usercenter.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shares")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @GetMapping("/{id}")
    public ShareDTO test(@PathVariable Integer id) {
        return shareService.findByIdWithFeign(id);
    }

}
