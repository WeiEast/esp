package com.apass.esp.web.offer;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.apass.esp.common.utils.JsonUtil;
import com.apass.esp.domain.Response;
import com.apass.esp.domain.vo.MyCouponVo;
import com.apass.esp.domain.vo.ProCouponVo;
import com.apass.esp.service.offer.CouponManagerService;
import com.apass.esp.service.offer.MyCouponManagerService;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.GsonUtils;

@Controller
@RequestMapping("/my/coupon")
public class MyCouponManagerController {

	private static final Logger logger = LoggerFactory.getLogger(MyCouponManagerController.class);
	
	@Autowired
	private MyCouponManagerService myCouponManagerService;
	
	@Autowired
	private CouponManagerService couponManagerService;
	
	/**
	 * 我的优惠券
	 * @param paramMap
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/list")
	public Response getMyCoupons(@RequestBody Map<String, Object> paramMap){
		logger.info("getMyCoupons:--------->{}",GsonUtils.toJson(paramMap));
		String userId = CommonUtils.getValue(paramMap, "userId");
		if(StringUtils.isBlank(userId)){
			logger.error("用户编号不能为空!");
			return Response.fail("用户编号不能为空!");
		}
		try {
			Map<String,Object> params = myCouponManagerService.getCoupons(userId);
			/**
			 * sprint 11 您还有未领取的券,如果size 0 则前端不显示
			 */
			List<ProCouponVo> proCouponRelList=couponManagerService.getCouponList(Long.parseLong(userId));
			params.put("couponsize", proCouponRelList.size());
			return Response.successResponse(params);
		} catch (Exception e) {
			logger.error("get mycoupons is failed------{}", e);
		}
		return Response.fail("我的优惠券获取失败!");
	}
	
	@ResponseBody
	@RequestMapping("/saveCoupon")
	public Response giveCouponToUser(@RequestBody Map<String, Object> paramMap){
		String userId = CommonUtils.getValue(paramMap, "userId");
		String activityId = CommonUtils.getValue(paramMap, "activityId");
		String couponId = CommonUtils.getValue(paramMap, "couponId");
		if(StringUtils.isBlank(activityId)){
			logger.error("活动编号不能为空!");
			return Response.fail("活动编号不能为空!");
		}
		logger.info("getGroupAndGoodsByGroupId:--------->{}",GsonUtils.toJson(paramMap));
		try {
			int count = myCouponManagerService.giveCouponToUser(new MyCouponVo(Long.parseLong(userId),Long.parseLong(couponId),Long.parseLong(activityId)));
			if(count > 0){
				return Response.success("领取成功!");
			}
		} catch(BusinessException e){
			logger.error("business giveCouponToUser :{}",e);
			return Response.fail(e.getErrorDesc());
		} catch (Exception e) {
			logger.error("exception giveCouponToUser :{}",e);
		}
		return Response.fail("当前领券人数过多，请稍后再试!");
	}
	
	@ResponseBody
	@RequestMapping("/deleteMyCoupon")
	public Response deleteMyCoupon(@RequestBody Map<String, Object> paramMap){
		logger.info("deleteMyCoupon---------------------->{}",JsonUtil.toJsonString(paramMap));
		String mycouponId = CommonUtils.getValue(paramMap, "mycouponId");
		if(StringUtils.isEmpty(mycouponId)){
			return Response.fail("参数传递有误!");
		}
		try {
			myCouponManagerService.deleteMyCoupon(mycouponId);
			return Response.success("删除成功!");
		} catch (Exception e) {
			logger.error("exception giveCouponToUser :{}",e);
			return Response.fail("删除失败!");
		}
	}
	  /**
     * 您还有优惠券未领取
     * @param paramMap
     * @return
     */
	@ResponseBody
	@RequestMapping("/noGetCoupons")
	public Response noGetCoupons(@RequestBody Map<String, Object> paramMap){
		String userId = CommonUtils.getValue(paramMap, "userId");
		if(StringUtils.isBlank(userId)){
			logger.error("用户id不能为空!");
			return Response.fail("用户id不能为空!");
		}
		List<ProCouponVo> proCouponRelList=couponManagerService.getCouponList(Long.parseLong(userId));
		 return Response.success("加载未领取优惠券成功!",proCouponRelList);
	}


//	@ResponseBody
//	@RequestMapping("/saveCouponFromScan")
//	public Response saveCouponFromScan(@RequestBody Map<String, Object> paramMap){
//		String userId = CommonUtils.getValue(paramMap, "userId");
//		String activityId = CommonUtils.getValue(paramMap, "activityId");
//		String telephone = CommonUtils.getValue(paramMap, "telephone");
//		if(StringUtils.isBlank(activityId)){
//			logger.error("活动编号不能为空!");
//			return Response.fail("活动编号不能为空!");
//		}
//		logger.info("saveCouponFromScan:--------->参数：{}",GsonUtils.toJson(paramMap));
//		try {
//			int count = myCouponManagerService.saveCouponToUserFromScan(Long.parseLong(userId),Long.parseLong(activityId),telephone);
//			if(count > 0){
//				return Response.success("领取成功!");
//			}
//		} catch(BusinessException e){
//			logger.error("business saveCouponFromScan :{}",e);
//			return Response.fail(e.getErrorDesc());
//		} catch (Exception e) {
//			logger.error("exception saveCouponFromScan :{}",e);
//		}
//		return Response.fail("服务器忙，请稍后再试！");
//	}

	/**
	 * 扫码领优惠券接口：扫再次对应不同优惠券
	 * @param paramMap
	 * @return
     */
	@ResponseBody
	@RequestMapping("/saveCouponFromScan")
	public Response saveTwoCouponFromScan(@RequestBody Map<String, Object> paramMap){
		try {
			String userId = CommonUtils.getValue(paramMap, "userId");
			String activityId = CommonUtils.getValue(paramMap, "activityId");
			String telephone = CommonUtils.getValue(paramMap, "telephone");
			//多个优惠券id拼接成字符串，用逗号隔开222,334,225
			String couponIds = CommonUtils.getValue(paramMap, "couponIds");
			int count = 0;
			if(StringUtils.isBlank(activityId)){
				logger.error("活动编号不能为空!");
				return Response.fail("活动编号不能为空!");
			}
			logger.info("saveCouponFromScan:--------->参数：{}",GsonUtils.toJson(paramMap));
			if(StringUtils.isEmpty(couponIds)){
				//领所有优惠券
				count = myCouponManagerService.saveCouponToUserFromScan(Long.parseLong(userId),Long.parseLong(activityId),telephone);
			}else{
				count = myCouponManagerService.saveCouponToUserFromScan(Long.parseLong(userId),Long.parseLong(activityId),telephone,couponIds);
			}

			if(count > 0){
				return Response.success("领取成功!");
			}
		} catch(BusinessException e){
			logger.error("business saveCouponFromScan :{}",e);
			return Response.fail(e.getErrorDesc());
		} catch (Exception e) {
			logger.error("exception saveCouponFromScan :{}",e);
		}

		return Response.fail("服务器忙，请稍后再试！");
	}

}
