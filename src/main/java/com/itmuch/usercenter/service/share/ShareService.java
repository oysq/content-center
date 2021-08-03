package com.itmuch.usercenter.service.share;

import com.itmuch.usercenter.dao.RocketMQTxMapper;
import com.itmuch.usercenter.dao.ShareMapper;
import com.itmuch.usercenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.usercenter.domain.dto.content.ShareDTO;
import com.itmuch.usercenter.domain.dto.message.UserAddBonusMessage;
import com.itmuch.usercenter.domain.dto.user.UserDTO;
import com.itmuch.usercenter.domain.entity.RocketMQTx;
import com.itmuch.usercenter.domain.entity.Share;
import com.itmuch.usercenter.domain.enums.AuditStatusEnum;
import com.itmuch.usercenter.feignclient.UserCenterFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

    @Autowired
    private RocketMQTxMapper rocketMQTxMapper;

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
     * @param shareId
     * @param paramDTO
     * @return
     */
    public Share auditById(String shareId, ShareAuditDTO paramDTO) {

        // 是否存在
        Share share = shareMapper.selectById(shareId);
        if(share == null) {
            throw new IllegalArgumentException("分享的主键id不存在");
        }

        // 是否待审核
        if(!Objects.equals(share.getAuditStatus(), AuditStatusEnum.NOT_YET.toString())) {
            throw new IllegalArgumentException("分享单已审核，请勿重复审批");
        }

        // 如果是通过的话，给作者加积分
        if(Objects.equals(paramDTO.getAuditStatus(), AuditStatusEnum.PASS)) {

            // 发送半消息，走事务
            this.sendMessageWithTx(share.getUserId(), shareId, paramDTO);

        } else {
            // 审核不通过，无需发送消息，不走事务
            this.updateDb(shareId, paramDTO);
        }

        // 返回
        return share;
    }

    /**
     * 直接发送消息，不走事务
     * @param userId
     */
    private void sendMessage(Integer userId) {
        rocketMQTemplate.convertAndSend(
                "add-bonus",
                UserAddBonusMessage
                        .builder()
                        .userId(userId)
                        .bonus(50)
                        .build()
        );
    }

    /**
     * 发送半消息，走事务
     * @param userId
     * @param paramDTO
     */
    private void sendMessageWithTx(Integer userId, String shareId, ShareAuditDTO paramDTO) {
        rocketMQTemplate.sendMessageInTransaction(
                "tx-add-bonus-group",
                "add-bonus",
                MessageBuilder
                        .withPayload(
                                UserAddBonusMessage
                                        .builder()
                                        .userId(userId)
                                        .bonus(50)
                                        .build()
                        )
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, UUID.randomUUID().toString())
                        .setHeader("share_id", shareId)
                        .build(),
                paramDTO
        );
    }

    /**
     * 更新分享的状态
     * @param shareId
     * @param paramDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDb(String shareId, ShareAuditDTO paramDTO) {
        // 更新审核状态
        Share share = Share.builder()
                .id(shareId)
                .auditStatus(paramDTO.getAuditStatus().toString())
                .reason(paramDTO.getReason())
                .build();
        shareMapper.updateById(share);
    }

    /**
     * 更新分享的状态，并记录事务标记
     * @param shareId
     * @param paramDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDbWithTx(String shareId, ShareAuditDTO paramDTO, String transactionId) {
        // 更新审核状态
        this.updateDb(shareId, paramDTO);
        // 记录事务标记
        rocketMQTxMapper.insert(
                RocketMQTx.builder()
                    .transactionId(transactionId)
                    .log("事务执行完成")
                    .build()
        );
    }

}
