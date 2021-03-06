package com.apass.esp.domain.entity.activity;
import java.util.Date;

import com.apass.esp.domain.entity.LimitGoodsSku;
import com.apass.esp.domain.enums.GoodStatus;
/**
 * 限时购活动商品  后台查询专用  冗余商品基本信息表相关字段
 * @author Administrator
 *
 */
public class LimitGoodsSkuVo extends LimitGoodsSku{
    //冗余商品表字段     //商品活动价上传 //商品市场价冗余
    private String goodsName;//商品名称
    private String goodsCode;//商品编号
    private String merchantName;//商户编号
    private String status;//商品状态
    private String statusDesc;//商品状态描述
    private Date listTime;//上下架时间
    private Date delistTime;
    private Long categoryId1;//商品一级分类
    private Long limitPersonNum;//个人在当前活动还剩多少限购数量
    private Long time;//离活动开始时间或活动开始时间与服务器时间差
    private Date startTime;
    private Date endTime;
    private String limitFalg;//活动标准（活动未开始NotBeginning；活动进行中  InProgress）
    //冗余类目名称字段
    private String categoryId1Name;//商品一级分类名称
    //冗余库存表字段
    private Long stockCurrAmt;//剩余库存
    //冗余商品来源
    private String source;
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
        this.statusDesc = statusDesc;
    }
    public Date getListTime() {
        return listTime;
    }
    public void setListTime(Date listTime) {
        this.listTime = listTime;
    }
    public Date getDelistTime() {
        return delistTime;
    }
    public void setDelistTime(Date delistTime) {
        this.delistTime = delistTime;
    }
    public Long getStockCurrAmt() {
        return stockCurrAmt;
    }
    public void setStockCurrAmt(Long stockCurrAmt) {
        this.stockCurrAmt = stockCurrAmt;
    }
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getLimitFalg() {
		return limitFalg;
	}
	public void setLimitFalg(String limitFalg) {
		this.limitFalg = limitFalg;
	}
	public Long getLimitPersonNum() {
		return limitPersonNum;
	}
	public void setLimitPersonNum(Long limitPersonNum) {
		this.limitPersonNum = limitPersonNum;
	}
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    
}