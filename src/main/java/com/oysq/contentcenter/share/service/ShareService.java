package com.oysq.contentcenter.share.service;

import com.oysq.contentcenter.feign.client.UserCenterFeignClient;
import com.oysq.contentcenter.share.dao.ShareMapper;
import com.oysq.contentcenter.share.domain.dto.ShareDTO;
import com.oysq.contentcenter.share.domain.entity.Share;
import com.oysq.contentcenter.user.domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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



}
