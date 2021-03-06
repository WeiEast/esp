package com.apass.esp.schedule;

import com.apass.esp.service.offer.ProGroupGoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by DELL on 2018/7/16.
 */
@Component
@Configurable
@EnableScheduling
@Profile("Schedule")
public class GoodsHandlerScheduleTask {
    private static final Logger logger = LoggerFactory.getLogger(GoodsHandlerScheduleTask.class);
    @Autowired
    private ProGroupGoodsService proGroupGoodsService;

    /**
     * 每1小时执行下架操作
     * 下架不符合下架系数的房易贷活动商品
     */
    @Scheduled(cron = "0 0 0/1 * * *")
    public void downProductOfFyd() {
        try {
            logger.info("-----开始下架房易贷专属活动下的商品");
            proGroupGoodsService.downProductOfFyd();
        } catch (Exception e) {
            logger.error("---------下架房易贷专属活动下的商品异常", e);
        }
    }

}
