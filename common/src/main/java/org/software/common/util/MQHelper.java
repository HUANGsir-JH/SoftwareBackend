package org.software.common.util;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MQHelper {

    @Value("${rocketmq.producer.send-message-timeout}")
    private Integer messageTimeOut;
    @Value("${rocketmq.topic}")
    private String topic;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public <T> SendResult syncSend(String tag, T value){
        return rocketMQTemplate.syncSend(topic + ":" + tag, JSONUtil.toJsonStr(value));
    }



}
