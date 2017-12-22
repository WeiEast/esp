package com.apass.esp.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

public class ProCoupon {
    private Long id;

    private String name;

    private String extendType;

    private String type;

    private Integer effectiveTime;

    private String sillType;

    private BigDecimal couponSill;

    private BigDecimal discountAmonut;

    private String categoryId1;

    private String categoryId2;

    private String goodsCode;

    private String similarGoodsCode;

    private String memo;

    private String createUser;

    private String updateUser;

    private Date createdTime;

    private Date updatedTime;

    private String isDelete;
    
    private Long brandId;

    private Long offerRange;
    
    private String categoryId3;

    private String skuId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtendType() {
        return extendType;
    }

    public void setExtendType(String extendType) {
        this.extendType = extendType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Integer effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getSillType() {
        return sillType;
    }

    public void setSillType(String sillType) {
        this.sillType = sillType;
    }

    public BigDecimal getCouponSill() {
        return couponSill;
    }

    public void setCouponSill(BigDecimal couponSill) {
        this.couponSill = couponSill;
    }

    public BigDecimal getDiscountAmonut() {
        return discountAmonut;
    }

    public void setDiscountAmonut(BigDecimal discountAmonut) {
        this.discountAmonut = discountAmonut;
    }

    public String getCategoryId1() {
        return categoryId1;
    }

    public void setCategoryId1(String categoryId1) {
        this.categoryId1 = categoryId1;
    }

    public String getCategoryId2() {
        return categoryId2;
    }

    public void setCategoryId2(String categoryId2) {
        this.categoryId2 = categoryId2;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getSimilarGoodsCode() {
        return similarGoodsCode;
    }

    public void setSimilarGoodsCode(String similarGoodsCode) {
        this.similarGoodsCode = similarGoodsCode;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
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

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getOfferRange() {
        return offerRange;
    }

    public void setOfferRange(Long offerRange) {
        this.offerRange = offerRange;
    }
    
    public String getCategoryId3() {
        return categoryId3;
    }

    public void setCategoryId3(String categoryId3) {
        this.categoryId3 = categoryId3;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }
}