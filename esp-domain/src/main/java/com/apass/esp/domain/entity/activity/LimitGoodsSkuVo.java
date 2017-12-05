package com.apass.esp.domain.entity.activity;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.apass.esp.domain.entity.LimitGoodsSku;
import com.apass.esp.domain.enums.GoodStatus;
public class LimitGoodsSkuVo extends LimitGoodsSku{
    //冗余商品表字段     //商品活动价上传 //商品市场价冗余
    private String goodsName;//商品名称
    private String goodsCode;//商品编号
    private String merchantName;//商户编号
    private Long categoryId1;//商品一级分类
    private String categoryId1Name;//商品一级分类名称
    private String status;//商品状态
    private String statusDesc;//商品状态描述
    private Date listTime;
    private String listTimeString;
    private Date delistTime;
    private String delistTimeString;
    //冗余库存表字段
    private Long stockCurrAmt;//剩余库存
    public String getGoodsName() {
        return goodsName;
    }
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }
    public String getGoodsCode() {
        return goodsCode;
    }
    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }
    public String getMerchantName() {
        return merchantName;
    }
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    public Long getCategoryId1() {
        return categoryId1;
    }
    public void setCategoryId1(Long categoryId1) {
        this.categoryId1 = categoryId1;
    }
    public String getCategoryId1Name() {
        return categoryId1Name;
    }
    public void setCategoryId1Name(String categoryId1Name) {
        this.categoryId1Name = categoryId1Name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatusDesc() {
        return statusDesc;
    }
    public void setStatusDesc(String statusDesc) {
        String content = "";
        GoodStatus[] goodsStatus = GoodStatus.values();
        for (GoodStatus goodStatus : goodsStatus) {
            if (goodStatus.getCode().equals(statusDesc)) {
                content = goodStatus.getMessage();
            }
        }
        this.statusDesc = content;
    }
    public Date getListTime() {
        return listTime;
    }
    public void setListTime(Date listTime) {
        this.listTime = listTime;
    }
    public String getListTimeString() {
        return listTimeString;
    }
    public void setListTimeString(String listTimeString) {
        this.listTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(listTime);
    }
    public Date getDelistTime() {
        return delistTime;
    }
    public void setDelistTime(Date delistTime) {
        this.delistTime = delistTime;
    }
    public String getDelistTimeString() {
        return delistTimeString;
    }
    public void setDelistTimeString(String delistTimeString) {
        this.delistTimeString = delistTimeString;
    }
    public Long getStockCurrAmt() {
        return stockCurrAmt;
    }
    public void setStockCurrAmt(Long stockCurrAmt) {
        this.stockCurrAmt = stockCurrAmt;
    }
}