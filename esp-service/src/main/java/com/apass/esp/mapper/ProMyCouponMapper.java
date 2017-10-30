package com.apass.esp.mapper;

import java.util.List;

import com.apass.esp.domain.entity.ProMyCoupon;
import com.apass.esp.domain.query.ProMyCouponQuery;
import com.apass.gfb.framework.mybatis.GenericMapper;

/**
 * Created by jie.xu on 17/10/27.
 */
public interface ProMyCouponMapper extends GenericMapper<ProMyCoupon, Long> {
	
	/**
	 * 根据用户的Id和活动、优惠券关联表Id查询对应的信息
	 * @param query
	 * @return
	 */
	List<ProMyCoupon> getCouponByUserIdAndRelId(ProMyCouponQuery query);
	
	/**
	 * 根据条件获取客户券的信息
	 */
	List<ProMyCoupon> getCouponByStatusAndDate(ProMyCouponQuery query);
	/**
	 * 根据用户的Id和优惠券Id查询对应的信息
	 * @param query
	 * @return
	 */
	List<ProMyCoupon> getCouponByUserIdAndCouponId(ProMyCouponQuery query);
}