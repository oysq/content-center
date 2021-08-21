package com.oysq.contentcenter.rocketmq.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oysq.contentcenter.rocketmq.dao.RocketMQTxMapper;
import com.oysq.contentcenter.share.domain.dto.ShareAuditDTO;
import com.oysq.contentcenter.rocketmq.domain.entity.RocketMQTx;
import com.oysq.contentcenter.share.service.ShareAdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

@Slf4j
@RocketMQTransactionListener(txProducerGroup = "tx-add-bonus-group")
public class AddBonusTransactionListener implements RocketMQLocalTransactionListener {

    @Autowired
    private ShareAdminService shareAdminService;

    @Autowired
    private RocketMQTxMapper rocketMQTxMapper;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {

        String shareId = (String) message.getHeaders().get("share_id");
        String transactionId = (String) message.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);

        try {
            shareAdminService.updateDbWithTx(shareId, (ShareAuditDTO) o, transactionId);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("本地事务执行失败", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {

        String transactionId = (String) message.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);

        if(rocketMQTxMapper.selectCount(new QueryWrapper<RocketMQTx>().eq("transaction_id", transactionId)) > 0) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }

}
