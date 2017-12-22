package com.apass.esp.service.offer;

import com.apass.esp.domain.entity.ProActivityCfg;
import com.apass.esp.domain.entity.ProCoupon;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.enums.CouponExtendType;
import com.apass.esp.domain.enums.CouponType;
import com.apass.esp.domain.enums.SourceType;
import com.apass.esp.domain.query.ProCouponQuery;
import com.apass.esp.mapper.ProCouponMapper;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.jd.JdGoodsInfoService;
import com.apass.gfb.framework.mybatis.page.Pagination;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by xiaohai on 2017/10/30.
 */
@Service
@Transactional
public class ProCouponService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProCouponService.class);
    @Autowired
    private ProCouponMapper couponMapper;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private JdGoodsInfoService jdGoodsInfoService;
    @Autowired
    private ActivityCfgService activityCfgService;

    /**
     * 分页查询优惠券列表
     * @param paramMap
     * @return
     */
    public Pagination<ProCoupon> pageList(ProCouponQuery query) {
        Pagination<ProCoupon> pagination = new Pagination<>();
        List<ProCoupon> proCouponList = couponMapper.pageList(query);
        if(CollectionUtils.isNotEmpty(proCouponList)){
            for (ProCoupon proCoupon:proCouponList) {
                for (CouponExtendType couponExtendType : CouponExtendType.values()) {
                    if(StringUtils.equalsIgnoreCase(proCoupon.getExtendType(),couponExtendType.getCode())){
                        proCoupon.setExtendType(couponExtendType.getMessage());
                    }
                }

                for (CouponType couponType : CouponType.values()) {
                    if(StringUtils.equalsIgnoreCase(proCoupon.getType(),couponType.getCode())){
                        proCoupon.setType(couponType.getMessage());
                    }
                }
            }

        }
        Integer count = couponMapper.pageListCount(query);
        pagination.setDataList(proCouponList);
        pagination.setTotalCount(count);
        return pagination;
    }
    /**
     * 根据商品code查询优惠券
     * @return
     */
    public List<ProCoupon> getProCouponList(ProCoupon proCoupon) {
        return couponMapper.getProCouponBCoupon(proCoupon);
    }

    public Integer inserProcoupon(ProCoupon proCoupon) {
    	/**
    	 * 如果是指定商品的优惠券
    	 */
        if(StringUtils.equals(proCoupon.getType(), CouponType.COUPON_ZDSP.getCode())){
            GoodsInfoEntity goodsInfoEntity = goodsService.selectGoodsByGoodsCode(proCoupon.getGoodsCode());
            if(goodsInfoEntity == null){
                throw new RuntimeException("商品编号输入错误，请重新输入");
            }

            //如果是京东商品，查询相似goods_code用逗号隔开，插入表中作为similar_goods_code
            if(StringUtils.equals(goodsInfoEntity.getSource(),SourceType.JD.getCode())||StringUtils.equals(goodsInfoEntity.getSource(), SourceType.WZ.getCode())){
                TreeSet<String> skuIdSet = jdGoodsInfoService.getJdSimilarSkuIdList(goodsInfoEntity.getExternalId());
                List<String> skuIdList = new ArrayList<String> (skuIdSet);
                if(CollectionUtils.isEmpty(skuIdList)){
                    LOGGER.error("数据有误,京东商品无skuId,商品id为:{}",String.valueOf(goodsInfoEntity.getId()));
                    throw new RuntimeException("数据有误,京东商品无skuId");
                }
                List<GoodsInfoEntity> goodsList = goodsService.getGoodsListBySkuIds(skuIdList);
                StringBuffer similarGoodsCode = new StringBuffer();
                for (int i = 0; i <goodsList.size() ; i++) {
                    if(i<goodsList.size()-1){
                        similarGoodsCode.append(goodsList.get(i).getGoodsCode()+",");
                    }else {
                        similarGoodsCode.append(goodsList.get(i).getGoodsCode());
                    }
                }
                proCoupon.setSimilarGoodsCode(similarGoodsCode.toString());
            }else {
                //非京东商品把goods_code填充进similarGoodsCode
                proCoupon.setSimilarGoodsCode(proCoupon.getGoodsCode());
            }
        }
        /**
         * 如果是活动商品
         */
        if(StringUtils.equals(proCoupon.getType(), CouponType.COUPON_HDSP.getCode())){
        	
        	
        }

        ProCoupon coupon2 = new ProCoupon();
        coupon2.setName(proCoupon.getName());
        List<ProCoupon> couList = couponMapper.getProCouponBCoupon(coupon2);
        if(CollectionUtils.isNotEmpty(couList)){
            LOGGER.error("优惠券名称重复，name:{}",proCoupon.getName());
            throw new RuntimeException("优惠券名称已存在，不能重复！");
        }
        return couponMapper.insertSelective(proCoupon);
    }

    public ProCoupon selectProCouponByPrimaryID(Long couponId) {
        return couponMapper.selectByPrimaryKey(couponId);
    }

    public Integer deleteByCouponId(ProCoupon proCoupon) {
        if(StringUtils.isNotBlank(proCoupon.getId().toString())){
            if(StringUtils.equalsIgnoreCase(CouponExtendType.COUPON_YHLQ.getCode(),proCoupon.getExtendType())){
                //根据优惠券id关联查询 t_esp_pro_coupon_rel和t_esp_pro_activity_cfg,再判断当前是否在有效期内
                List<ProActivityCfg> proActivityCfgList = activityCfgService.selectProActivityCfgByEntity(proCoupon.getId());
                if(CollectionUtils.isNotEmpty(proActivityCfgList)){
                    for (ProActivityCfg proActivityCfg:proActivityCfgList) {
                        if(!(proActivityCfg.getStartTime().getTime()> new Date().getTime() ||
                                proActivityCfg.getEndTime().getTime()<new Date().getTime())){
                            throw new RuntimeException("该优惠券正在参与活动!");
                        }
                    }
                }
            }

            //物理删除
            proCoupon.setExtendType(null);
            return couponMapper.updateByPrimaryKeySelective(proCoupon);
        }else {
            throw new RuntimeException("优惠券id不存在！");
        }
    }

    public List<ProCoupon> selectProCouponByIds(ArrayList<Long> couponIdList) {
        Map<String,Object> paramMap = Maps.newHashMap();
        paramMap.put("ids",couponIdList);
        return couponMapper.selectProCouponByIds(paramMap);
    }
}
