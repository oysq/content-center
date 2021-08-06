package com.itmuch.usercenter.notice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itmuch.usercenter.notice.dao.NoticeMapper;
import com.itmuch.usercenter.notice.domain.entity.Notice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController("notice")
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    @GetMapping("/test")
    public List<Notice> test() {
        Notice notice = Notice.builder()
                .content("这是提示的内容")
                .showFlag(1)
                .createTime(new Date())
                .build();
        noticeMapper.insert(notice);

        QueryWrapper wrapper = new QueryWrapper<Notice>();
        return noticeMapper.selectList(wrapper);
    }

}
