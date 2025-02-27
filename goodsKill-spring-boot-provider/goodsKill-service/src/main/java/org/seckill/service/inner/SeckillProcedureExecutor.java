package org.seckill.service.inner;

import lombok.extern.slf4j.Slf4j;
import org.seckill.api.constant.SeckillStatusConstant;
import org.seckill.api.service.SeckillService;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.mp.dao.mapper.SeckillMapper;
import org.seckill.service.mq.ActiveMqMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author heng
 */
@Slf4j
@Service
public class SeckillProcedureExecutor implements SeckillExecutor {

    @Autowired
    private SeckillMapper seckillMapper;
    @Autowired
    private ActiveMqMessageSender activeMqMessageSender;
    @Autowired
    private SeckillService seckillService;

    /**
     * 发送秒杀成功通知次数
     */
    private AtomicInteger sendTopicTimes = new AtomicInteger(0);

    /**
     * 处理用户秒杀请求
     *
     * @param seckillId 秒杀活动id
     * @param userPhone
     * @param note      秒杀备注信息
     */
    @Override
    public void dealSeckill(long seckillId, String userPhone, String note) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            SuccessKilled successKilled = new SuccessKilled();
            successKilled.setSeckillId(seckillId);
            successKilled.setUserPhone(userPhone);
            successKilled.setCreateTime(new Date());
            successKilled.setServerIp(localHost.getHostAddress() + ":" + localHost.getHostName());
            if (seckillService.reduceNumber(successKilled) < 1) {
                Seckill seckill = seckillMapper.selectById(seckillId);
                log.debug("当前库存：{}", seckill.getNumber());
                // 高并发时限制只能发一次秒杀完成通知
                if (!SeckillStatusConstant.END.equals(seckill.getStatus()) && sendTopicTimes.getAndIncrement() == 0) {
                    activeMqMessageSender.sendSeckillSuccessTopic(seckillId, note);
                    Seckill sendTopicResult = new Seckill();
                    sendTopicResult.setSeckillId(seckillId);
                    sendTopicResult.setStatus(SeckillStatusConstant.END);
                    seckillMapper.updateById(sendTopicResult);
                    // 重置发送成功通知次数
                    sendTopicTimes.set(0);
                }
                if (seckill.getNumber() <= 0) {
                    log.debug("库存不足，无法继续秒杀！");
                }
            }
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }
    }

}