package com.apass.esp.domain.entity;
import java.util.Date;
public class LimitUserMessage {
    private Long id;
    private Long limitBuyActId;
    private Long limitGoodsSkuId;
    private Long userId;
    private Long telephone;
    private Date createdTime;
    private Date updatedTime;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getLimitBuyActId() {
        return limitBuyActId;
    }
    public void setLimitBuyActId(Long limitBuyActId) {
        this.limitBuyActId = limitBuyActId;
    }
    public Long getLimitGoodsSkuId() {
        return limitGoodsSkuId;
    }
    public void setLimitGoodsSkuId(Long limitGoodsSkuId) {
        this.limitGoodsSkuId = limitGoodsSkuId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getTelephone() {
        return telephone;
    }
    public void setTelephone(Long telephone) {
        this.telephone = telephone;
    }
    public Date getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    public Date getUpdatedTime() {
        return updatedTime;
    }
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}