package com.itmuch.usercenter.sentinel.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.itmuch.usercenter.sentinel.service.SentinelTestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class SentinelTestController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SentinelTestService sentinelTestService;

    @GetMapping("/test5")
    public String test5() {
        return "test5";
    }

    @GetMapping("/test6")
    public String test6() throws InterruptedException {
        for(int i = 0 ; i < 20 ; i ++) {
            log.info("test ==> {}", i);
            restTemplate.getForObject("http://content-center:9091/test5", String.class);
            Thread.sleep(500);
        }
        return "模拟结束";
    }

    @GetMapping("/test7")
    public String test7() {
        this.sentinelTestService.sentinelResource();
        return "test7";
    }

    @GetMapping("/test8")
    public String test8() {
        this.sentinelTestService.sentinelResource();
        return "test8";
    }

    @GetMapping("/test9")
    public String test9() {
        for(int i = 0 ; i < 20 ; i ++) {
            log.info("test9 ==> {}", i);
            String res = restTemplate.getForObject("http://content-center:9091/test10/"+i, String.class);
            log.info("test9 <== {}", res);
        }
        return "模拟结束";
    }

    @GetMapping("/test10/{id}")
    public String test10(@PathVariable Integer id) throws InterruptedException {
        log.info("test5 rcv {}", id);
        Thread.sleep(2000);
        return "res:"+id;
    }

    /**
     * 热点参数
     * @param a
     * @param b
     * @return
     */
    @GetMapping("test11")
    @SentinelResource("hot")
    public String test11(@RequestParam(required = false) String a,
                         @RequestParam(required = false) String b) {
        return a+" "+b;
    }

    /**
     * java方式设置规则
     * @return
     */
    @GetMapping("test12")
    public String test12() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule("/shares/{id}");
        // set limit qps to 20
        rule.setCount(2);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        rules.add(rule);
        FlowRuleManager.loadRules(rules);

        return "success";
    }

    /**
     * Sentinel API
     * @param a
     * @return
     */
    @GetMapping("test13")
    public String test13(@RequestParam(required = false) String a) {

        String resourceName = "test-sentinel-api";

        // 定义来源
        ContextUtil.enter(resourceName, "xxx-wfw");

        Entry entry = null;
        try {

            // 定义资源
            entry = SphU.entry(resourceName);

            // 业务代码
            if(StringUtils.isBlank(a)) {
                throw new IllegalAccessException("参数a不可空");
            }
            return "success: "+a;
        } catch (BlockException e) {
            log.error("限流或降级了");
            return "限流或降级了";
        } catch (IllegalAccessException e2) {
            Tracer.trace(e2);
            return "参数非法";
        } finally {
            if(entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }

    }

    /**
     * Sentinel 注解
     * @param a
     * @return
     */
    @GetMapping("test14")
    @SentinelResource(
            value = "test-sentinel-annotation",// 资源
            blockHandler = "sentinelBlockHandler",// 处理限流或降级
            fallback = "sentinelFallBackHandler"// 处理降级
    )
    public String test14(@RequestParam(required = false) String a) throws IllegalAccessException {
        // 业务代码
        if(StringUtils.isBlank(a)) {
            throw new IllegalAccessException("参数a不可空");
        }
        return "success: "+a;
    }

    /**
     * 处理限流或降级
     * 必须是public，返回值类型和入参必须和 test14 相同
     */
    public String sentinelBlockHandler(String a, BlockException e) {
        log.error("sentinelBlockHandler 限流或降级了", e);
        return "sentinelBlockHandler 限流或降级了";
    }

    /**
     * 处理降级
     * 必须是public，返回值类型和入参必须和 test14 相同
     */
    public String sentinelFallBackHandler(String a) {
        log.error("sentinelFallBackHandler 降级了");
        return "sentinelFallBackHandler 降级了";
    }

}
