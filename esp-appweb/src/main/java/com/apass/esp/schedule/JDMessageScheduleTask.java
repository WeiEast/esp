package com.apass.esp.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.apass.esp.domain.enums.JdMessageEnum;
import com.apass.esp.mq.listener.JDTaskAmqpAccess;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.third.party.jd.client.JdMessager;
import com.apass.esp.third.party.jd.entity.base.JdApiMessage;
import com.apass.esp.third.party.weizhi.client.WeiZhiMessageClient;
import com.apass.gfb.framework.environment.SystemEnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * type: class
 * 京东消息接收
 *
 * @author xianzhi.wang
 * @see
 * @since JDK 1.8
 */
@Component
@Configurable
@EnableScheduling
@Profile("Schedule")
public class JDMessageScheduleTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDMessageScheduleTask.class);

    @Autowired
    private JDTaskAmqpAccess jdTaskAmqpAccess;

    @Autowired
    private WeiZhiMessageClient weiZhiMessageClient;

    @Scheduled(cron = "0 0/30 * * * *")
    public void handleJDMessageScheduleTask() {
     List<JdMessageEnum> messageEnumList = new ArrayList<>();
        messageEnumList.add(JdMessageEnum.DELIVERED_ORDER);
        messageEnumList.add(JdMessageEnum.WITHDRAW_SKU);
        messageEnumList.add(JdMessageEnum.DELETEADD_SKU);
        messageEnumList.add(JdMessageEnum.PRICE_SKU);

        List<JdApiMessage> jdApiMessageList = null;
        for(JdMessageEnum messageEnum : messageEnumList){
            try {
                jdApiMessageList = weiZhiMessageClient.getMsg(messageEnum.getType());
            } catch (Exception e) {
                LOGGER.error("handleJDMessageScheduleTask getMessage error...");
                return;
            }
            for (JdApiMessage jdApiMessage : jdApiMessageList ) {
                try {
                    jdTaskAmqpAccess.directSend(jdApiMessage);
                } catch (Exception e) {
                    LOGGER.info("handleJDMessageScheduleTask jdApiMessage", jdApiMessage.getType(), jdApiMessage.getResult());
                    continue;
                }
                if(jdApiMessage.getType() != JdMessageEnum.DELIVERED_ORDER.getType()){
                    weiZhiMessageClient.delMsg(jdApiMessage.getId(),jdApiMessage.getType());
                }else if(jdApiMessage.getType() == JdMessageEnum.DELIVERED_ORDER.getType()){
                    JSONObject result = jdApiMessage.getResult();
                    JSONArray jsonArray = result.getJSONArray("orderTrack");
                    for(Object jo : jsonArray){
                        JSONObject orderTrack = (JSONObject) jo;
                        Long id = orderTrack.getLong("id");
                        weiZhiMessageClient.delMsg(id,JdMessageEnum.DELIVERED_ORDER.getType());
                    }
                }
            }
        }
    }
}
