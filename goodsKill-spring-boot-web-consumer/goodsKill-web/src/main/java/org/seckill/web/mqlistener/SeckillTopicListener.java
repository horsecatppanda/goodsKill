package org.seckill.web.mqlistener;//package org.seckill.web.mqlistener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.seckill.api.enums.SeckillSolutionEnum;
import org.seckill.api.service.SeckillService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Date;

/**
 * 秒杀活动结束通知监听器
 * Created by heng on 18/09/02.
 */
@Slf4j
@Component
public class SeckillTopicListener implements MessageListener {
    @Reference
    private SeckillService seckillService;

    @SneakyThrows
    @Override
    @JmsListener(destination = "seckillTopic")
    public void onMessage(Message message) {
        long seckillId = 0;
        boolean status = false;
        String note = null;
        try {
            seckillId = message.getLongProperty("seckillId");
            status = message.getBooleanProperty("status");
            note = message.getStringProperty("note");
        } catch (JMSException e) {
            log.error(e.getMessage(), e);
        }
        if (status) {
            log.info("秒杀活动结束，{}时间：{},秒杀id：{}", new Object[]{note, new Date(), seckillId});
            long successKillCount = seckillService.getSuccessKillCount(seckillId);
            long temp = 0;
            while (successKillCount != temp) {
                // 计算总数可能有延迟，等待统计数据稳定后得到最终结果
                log.info("最终成功交易笔数统计中。。。");
                successKillCount = temp;
                Thread.sleep(1500L);
                temp = seckillService.getSuccessKillCount(seckillId);
            }
            log.info("最终成功交易笔数：{}", successKillCount);
            // 场景八由于是异步插入，补偿查询最新插入记录数，延迟10s
            try {
                if (SeckillSolutionEnum.REDIS_MONGO_REACTIVE.getName().endsWith(note)) {
                    Thread.sleep(10000);
                    log.info("mongo最终成功交易笔数：{}", successKillCount);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
