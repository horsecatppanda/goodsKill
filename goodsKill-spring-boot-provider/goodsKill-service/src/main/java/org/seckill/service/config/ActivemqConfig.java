package org.seckill.service.config;

import lombok.Setter;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.seckill.service.mq.SeckillActiveConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author techa03
 * @date 2020/6/27
 */
@Configuration
@ConfigurationProperties(prefix = "spring.activemq")
public class ActivemqConfig {

    @Setter
    private String brokerUrl;

    private ActiveMQConnectionFactory targetConnectionFactory;

    @Bean
    public ActiveMQQueue queueDestination() {
        ActiveMQQueue goodsKill = new ActiveMQQueue("goodsKill");
        return goodsKill;
    }

    @Bean
    public ActiveMQTopic topicDestination() {
        return new ActiveMQTopic("seckillTopic");
    }

    @Bean
    public JmsTemplate jmsTopicTemplate() {
        JmsTemplate jmsTopicTemplate = new JmsTemplate();
        jmsTopicTemplate.setConnectionFactory(targetConnectionFactory());
        jmsTopicTemplate.setDefaultDestination(topicDestination());
        return jmsTopicTemplate;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(targetConnectionFactory());
        jmsTemplate.setDefaultDestination(queueDestination());
        return jmsTemplate;
    }

    public synchronized ActiveMQConnectionFactory targetConnectionFactory() {
        if (targetConnectionFactory != null ) {
            return targetConnectionFactory;
        }
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        this.targetConnectionFactory = connectionFactory;
        return connectionFactory;
    }

    @Bean
    public DefaultMessageListenerContainer jmsContainer(SeckillActiveConsumer seckillActiveConsumer) {
        DefaultMessageListenerContainer jmsContainer = new DefaultMessageListenerContainer();
        jmsContainer.setConnectionFactory(targetConnectionFactory());
        jmsContainer.setDestination(queueDestination());
        jmsContainer.setMessageListener(seckillActiveConsumer);
        return jmsContainer;
    }

    @Bean
    public ThreadPoolExecutor taskExecutor() {
        return new ThreadPoolExecutor(10, 20, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
