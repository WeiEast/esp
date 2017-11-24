package com.apass.esp.domain.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @description 订单状态枚举
 * 
 * @author chenbo
 * @version $Id: OrderStatus.java, v 0.1 2016年12月19日 下午3:15:10 chenbo Exp $
 */
public enum OrderStatus {
    
    ORDER_NOPAY("D00","待付款"),
    
    ORDER_PAYED("D02","待发货"),
    
    ORDER_SEND("D03","待收货"),
    
    ORDER_COMPLETED("D04","交易完成"),
    
    ORDER_RETURNING("D05","售后服务中"),
    
    //ORDER_RETURNED("D06","售后完成"),
    
    ORDER_CANCEL("D07","订单失效"),
    
    ORDER_DELETED("D08","订单删除"),
    
    ORDER_REFUNDPROCESSING("D09","退款处理中"),
    
    ORDER_TRADCLOSED("D10","交易关闭");
    
    private String code;
    
    private String message;
    
    private OrderStatus(String code ,String message){
        this.code=code;
        this.message=message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public static boolean isContail(String code){
    	return Arrays.asList(new String[]{OrderStatus.ORDER_PAYED.getCode(),
    			OrderStatus.ORDER_SEND.getCode(),OrderStatus.ORDER_COMPLETED.getCode(),
    			OrderStatus.ORDER_RETURNING.getCode(),OrderStatus.ORDER_REFUNDPROCESSING.getCode()}).contains(code);
    }
}
