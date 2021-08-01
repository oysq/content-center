package com.itmuch.usercenter.service.share;

import com.itmuch.usercenter.dao.ShareMapper;
import com.itmuch.usercenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.usercenter.domain.dto.content.ShareDTO;
import com.itmuch.usercenter.domain.dto.message.UserAddBonusMessage;
import com.itmuch.usercenter.domain.dto.user.UserDTO;
import com.itmuch.usercenter.domain.entity.Share;
import com.itmuch.usercenter.domain.enums.AuditStatusEnum;
import com.itmuch.usercenter.feignclient.UserCenterFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShareService {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private UserCenterFeignClient userCenterFeignClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public ShareDTO findByIdWithFeign(Integer id) {
        Share share = shareMapper.selectById(id);
        Integer userId = share.getUserId();

        UserDTO userDTO = userCenterFeignClient.findById(userId);

        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());

        return shareDTO;
    }

    public ShareDTO findByIdWithRibbon(Integer id) {
        Share share = shareMapper.selectById(id);
        Integer userId = share.getUserId();

        UserDTO userDTO = restTemplate.getForObject(
                "http://user-center/users/{id}",
                UserDTO.class,
                userId
        );

        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());

        return shareDTO;
    }

    public ShareDTO findById(Integer id) {
        Share share = shareMapper.selectById(id);
        Integer userId = share.getUserId();

        List<ServiceInstance> instanceList = discoveryClient.getInstances("user-center");
        List<String> targetUrls = instanceList.stream()
                .map(instance -> instance.getUri().toString())
                .collect(Collectors.toList());

        String targetUrl = targetUrls.get(RandomUtils.nextInt(targetUrls.size()));

        log.info("目标地址: " + targetUrl);

        UserDTO userDTO = restTemplate.getForObject(
                targetUrl + "/users/{id}",
                UserDTO.class,
                userId
        );

        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());

        return shareDTO;
    }

    /**
     * 根据 id 审核
     * @param id
     * @param shareAuditDTO
     * @return
     */
    public Share auditById(String id, ShareAuditDTO shareAuditDTO) {

        // 是否存在
        Share share = shareMapper.selectById(id);
        if(share == null) {
            throw new IllegalArgumentException("分享的主键id不存在");
        }

        // 是否待审核
        if(!Objects.equals(share.getAuditStatus(), AuditStatusEnum.NOT_YET.toString())) {
            throw new IllegalArgumentException("分享单已审核，请勿重复审批");
        }

        // 更新审核状态
        share.setAuditStatus(shareAuditDTO.getAuditStatus().toString());
        share.setReason(shareAuditDTO.getReason());
        shareMapper.updateById(share);

        // 如果是通过的话，给作者加积分
        if(Objects.equals(shareAuditDTO.getAuditStatus(), AuditStatusEnum.PASS)) {
            rocketMQTemplate.convertAndSend(
                    "add-bonus",
                    UserAddBonusMessage
                            .builder()
                            .userId(share.getUserId())
                            .bonus(50)
                            .build()
            );
        }

        // 返回
        return share;
    }

}
