package com.apass.esp.schedule;

import com.apass.esp.domain.entity.PAUser;
import com.apass.esp.domain.entity.customer.CustomerInfo;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.search.entity.Goods;
import com.apass.esp.search.enums.IndexType;
import com.apass.esp.search.manager.IndexManager;
import com.apass.esp.service.bill.CustomerServiceClient;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.home.PAUserService;
import com.apass.gfb.framework.utils.AESUtils;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * type: class
 * es初始化
 *
 * @author xianzhi.wang
 * @date 2017/8/21
 * @see
 * @since JDK 1.8
 */
@Component
@Configurable
@EnableScheduling
@Profile("Schedule")
public class PingAnTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingAnTask.class);

    @Autowired
    private PAUserService paUserService;

    @Autowired
    private CustomerServiceClient customerServiceClient;
    /**
     * 平安保险推送：每日凌晨跑定时任务
     * 思路：
     * 1，去平安表中查询 数据
     * 2，遍历，查看是否有身份证，有-->解析各个参数，调用平安接口。
     * 3，无--调占两个接口获取身份证号--解析调平安接口
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void esInitScheduleTask() {
       try{

           //查询平安表，一个星期内数据
           Date begin = DateFormatUtil.addDays(new Date(),-7);
           String startDate = DateFormatUtil.dateToString(begin)+" 00:00:00";
           String endDate = DateFormatUtil.dateToString(DateFormatUtil.addDays(new Date(),-1))+" 23:59:59";

           //根据时间区间查询
           List<PAUser> paUsers = paUserService.selectUserByRangeDate(startDate,endDate);
           if(CollectionUtils.isEmpty(paUsers)){
               return;
           }

           for(PAUser paUser : paUsers){
               String identity = paUser.getIdentity();
               if(StringUtils.isEmpty(identity)){
                   //调远程接口，获取identity
                   CustomerInfo customerInfo = customerServiceClient.getDouDoutCustomerInfo(paUser.getTelephone());
                   if(customerInfo == null){
                       customerInfo = customerServiceClient.getFydCustomerInfo(paUser.getTelephone());
                   }

                   paUser.setUsername(customerInfo.getRealName());
                   paUser.setIdentity(customerInfo.getIdentityNo());
               }

               paUser.setIdentity(identity);
               //推送数据至平安
               paUserService.saveToPAInterface(paUser);
           }

       }catch (Exception e){
           LOGGER.error("平安保险零点推送task异常,---Exception---",e);
       }

    }
}