package com.apass.esp.web.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.entity.AwardBindRel;
import com.apass.esp.domain.enums.AwardActivity.ActivityName;
import com.apass.esp.domain.vo.AwardActivityInfoVo;
import com.apass.esp.service.activity.AwardActivityInfoService;
import com.apass.esp.service.activity.AwardBindRelService;
import com.apass.esp.service.common.MobileSmsService;
import com.apass.esp.service.registerInfo.RegisterInfoService;
import com.apass.gfb.framework.cache.CacheManager;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.GsonUtils;
@RestController
@RequestMapping("/activity/regist")
public class RegisterInfoController {
    private static final Logger logger =  LoggerFactory.getLogger(RegisterInfoController.class);
    @Autowired
    private RegisterInfoService registerInfoService;
    /**
	 * 验证码工具
	 */
	@Autowired
	private MobileSmsService mobileRandomService;
	@Autowired
	private AwardBindRelService awardBindRelService;
	@Autowired
	private AwardActivityInfoService awardActivityInfoService;
	/**
	 * 缓存服务
	 */
	@Autowired
	private CacheManager cacheManager;
    /**
	 * <pre>
	 * 2.根据用户传递的手机号码查询用户表看是否是微信端用户
	 * &#64;param mobile
	 * &#64;return Responses
	 * </pre>
	 */
	@RequestMapping(value = "/isWeChatUser",method = RequestMethod.POST)
	public Response isWeChatUser(@RequestBody Map<String, Object> paramMap) {
		String mobile =  CommonUtils.getValue(paramMap, "mobile");//手机号
		String mobile2=mobile.replace(" ", "");
		Pattern p = Pattern.compile("^1[0-9]{10}$");
		Matcher m = p.matcher(mobile2);
 		if (StringUtils.isAnyBlank(mobile2)) {
			return Response.fail("手机号不能为空！");
		}else if (!m.matches()) {
			return Response.fail("手机号格式不正确,请重新输入！");
		}
		try {
			Response resp=registerInfoService.isWeChatUser(mobile2);
			if("1".equals(resp.getStatus())){
				Map<String,Object> resultMap=(Map<String, Object>) resp.getData();
				String falge=(String) resultMap.get("falge");
				Map<String,Object> respMap=new HashMap<String, Object>();
				respMap.put("weChatFalge", falge);
				return Response.success("手机号验证成功！",respMap);
			}
		} catch (Exception e) {
			logger.error("手机号验证失败", e);
			return Response.fail("手机号验证失败!");
		}
		return Response.fail("手机号验证失败！");
	}
	  /**
		 * <pre>
		 * 2.根据用户上传的身份证号判断是否与原用户表中的身份证号一致
		 * &#64;param mobile
		 * &#64;return Responses
		 * </pre>
		 */
		@RequestMapping(value = "/isSameIdentityNo",method = RequestMethod.POST)
		public Response isSameIdentityNo(@RequestBody Map<String, Object> paramMap) {

			String mobile =  CommonUtils.getValue(paramMap, "mobile");//手机号
			String identityNo =  CommonUtils.getValue(paramMap, "identityNo");//身份证号
			String mobile2=mobile.replace(" ", "");
			
			Pattern p = Pattern.compile("^1[0-9]{10}$");
			Matcher m = p.matcher(mobile2);
//			Pattern p2 = Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$");
//			Matcher m2 = p2.matcher(identityNo);
			if (StringUtils.isAnyBlank(mobile2)) {
				return Response.fail("手机号不能为空");
			}else if(!m.matches()){
				return Response.fail("手机号格式不正确,请重新输入");
			}else if(StringUtils.isAnyBlank(identityNo)){
				return Response.fail("身份证号不能为空");
			}
//			else if(!m2.matches()){
//				return Response.fail("身份证号格式不正确,请重新输入！");
//			}
			Map<String,Object> respMap=new HashMap<String,Object>();
			try {
				Response resp=registerInfoService.isWeChatUser(mobile2);
				if("1".equals(resp.getStatus())){
					Map<String,Object> resultMap=(Map<String, Object>) resp.getData();
					String identityNoReturn =(String) resultMap.get("identityNo");
					if(identityNo.equals(identityNoReturn)){
						respMap.put("identityFalge", "same");
					}else{
//						respMap.put("identityFalge", "different");
						return Response.fail("手机号"+mobile+"在微信公众号的注册信息与所输入的身份证号不符");
					}
					return Response.success("身份证号验证成功！",respMap);
				}
			} catch (Exception e) {
				logger.error("身份证号验证失败", e);
				return Response.fail("身份证号验证失败!");
			}
			return Response.fail("手机号"+mobile+"在微信公众号的注册信息与所输入的身份证号不符");
		}
	
    /**
	 * <pre>
	 * 2.根据用户传递的手机号码, 调用消息接口向该手机号码发送验证码
	 * &#64;param mobile
	 * &#64;return Responses
	 * </pre>
	 */
	@RequestMapping(value = "/send",method = RequestMethod.POST)
	public Response sendRandomCode(@RequestBody Map<String, Object> paramMap) {

		String mobile =  CommonUtils.getValue(paramMap, "mobile");//手机号
		String smsType = CommonUtils.getValue(paramMap, "smsType");//验证码类型
		String randomCode=CommonUtils.getValue(paramMap, "randomCode");//随机码
		String randomFlage= CommonUtils.getValue(paramMap, "randomFlage");//随机码标识


		String mobile2=mobile.replace(" ", "");

		Pattern p = Pattern.compile("^1[0-9]{10}$");
		Matcher m = p.matcher(mobile2);
		if (StringUtils.isAnyBlank(mobile2, smsType)) {
			return Response.fail("手机号不能为空");
		}else if(!m.matches()){
			return Response.fail("手机号格式不正确,请重新输入！");
		}else if (StringUtils.isBlank( randomCode)) {
			return Response.fail("验证码不能为空");
		}else if (StringUtils.isBlank( randomFlage)) {
			return Response.fail("验证码不正确");
		}
		try {
			String cacheKey = "activityRegistRandom_"+ randomFlage;
			String cacheJson = cacheManager.get(cacheKey);
			Map<String ,Object> cacheJsonMap = GsonUtils.convert(cacheJson);
			if(cacheJsonMap == null || !cacheJsonMap.containsKey("value")){
				return Response.fail("验证码不正确");
			}
			if(!StringUtils.equalsIgnoreCase((String)cacheJsonMap.get("value"), randomCode)){
				return Response.fail("验证码不正确");
			}
//		    Boolean Flage=registerInfoService.isSendMes(smsType, mobile2);
			//判断短信验证码是否有效
		    Boolean Flage= mobileRandomService.getCode(smsType, mobile2);
			if(Flage){
				mobileRandomService.sendMobileVerificationCode(smsType, mobile2);
				return Response.success("验证码发送成功,请注意查收");
			}
//			else{
//				return Response.fail("短信发送过于频繁，请稍后再试！");
//			}
			return Response.fail("发送验证码失败,请稍后再试");
		} catch (BusinessException e) {
			logger.error("mobile verification code send fail", e);
			return Response.fail("网络异常,发送验证码失败,请稍后再试");
		}
	}
	/**
	 * <pre>
	 * 3.根据输入的手机号码&验证码进行校验用户填写的验证码是否正确
	 * &#64;param mobile
	 * &#64;param randomCode
	 * </pre>
	 */
	@RequestMapping(value = "/validate",method = RequestMethod.POST)
	public Response validateRandomCode(@RequestBody Map<String, Object> paramMap) {
		String smsType =  CommonUtils.getValue(paramMap, "smsType");// 验证码类型
 		String mobile =   CommonUtils.getValue(paramMap, "mobile");// 手机号
		String code =     CommonUtils.getValue(paramMap, "code");//短信验证码
		String randomCode=CommonUtils.getValue(paramMap, "randomCode");//随机码
		String InviterId=  CommonUtils.getValue(paramMap, "InviterId");//邀请人的id
		String randomFlage= CommonUtils.getValue(paramMap, "randomFlage");//随机码标识
		String mobile2=mobile.replace(" ", "");

		Pattern p = Pattern.compile("^1[0-9]{10}$");
		Matcher m = p.matcher(mobile2);
		if (StringUtils.isBlank( smsType)) {
			return Response.fail("手机号验证码类型不能为空");
		}else if (StringUtils.isBlank( mobile2)) {
			return Response.fail("手机号不能为空");
		}else if(!m.matches()){
			return Response.fail("手机号格式不正确,请重新输入！");
		}else if (StringUtils.isBlank( code)) {
			return Response.fail("短信验证码不能为空");
		}else if (StringUtils.isBlank( randomCode)) {
			return Response.fail("随机码不能为空");
		}else if (StringUtils.isBlank( InviterId)) {
			return Response.fail("邀请人的id不能为空");
		}else if (StringUtils.isBlank( randomFlage)) {
			return Response.fail("随机码标识不能为空");
		}
		try {
			String cacheKey = "activityRegistRandom_"+ randomFlage;
			String cacheJson = cacheManager.get(cacheKey);
			Map<String ,Object> cacheJsonMap = GsonUtils.convert(cacheJson);
			if(cacheJsonMap == null || !cacheJsonMap.containsKey("value")){
				return Response.fail("验证码不正确");
			}
			if(!StringUtils.equalsIgnoreCase((String)cacheJsonMap.get("value"), randomCode)){
				return Response.fail("验证码不正确");
			}
	        Map<String,Object> respMap=new HashMap<String,Object>(); 
	        boolean result2 = mobileRandomService.mobileCodeValidate(smsType, mobile2, code);//判断短信验证码是否填写正确
	        if(!result2){
	        	return Response.fail("手机验证码不正确");
	        }
        	Response resp=registerInfoService.isNewCustomer(mobile2,InviterId);
    		if("1".equals(resp.getStatus())){
    			Map<String,Object> rrse=(Map<String, Object>) resp.getData();
    			String falge=(String) rrse.get("falge");
    			if("old".equals(falge)){//已经在App中注册成功
    				Boolean  userFlage =false;
    			    if(rrse.get("userId")!=null){
    			    	userFlage=InviterId.equals(rrse.get("userId").toString());
    			    }
    				if(userFlage){
    					return Response.fail("绑定关系失败,自己不能邀请自己！");
    				}
    				
    			    ActivityName activityName=ActivityName.INTRO;//获取活动名称
			        AwardActivityInfoVo aInfoVo=awardActivityInfoService.getActivityByName(activityName);
					if(null==aInfoVo){
						return Response.fail("绑定关系失败,无有效活动！");
					}
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					Date aStartDate=sdf.parse(aInfoVo.getaStartDate());
					Date nowtime=new Date();
					int result=nowtime.compareTo(aStartDate);
					if(result<0){
						return Response.fail("绑定关系失败,活动还未开始！");
					}
    				AwardBindRel abr=new AwardBindRel();
    				abr.setInviteMobile(mobile2);
    				abr.setActivityId(aInfoVo.getId());
    				Integer abrel=awardBindRelService.selectByMobileAndActivityId(abr);//判断是否已经被邀请
    				if(abrel==0){//当前活动下没有被邀请
    			        AwardBindRel aRel=new AwardBindRel();
        				aRel.setActivityId(aInfoVo.getId());
        				aRel.setUserId(Long.parseLong(InviterId));
        				aRel.setMobile(rrse.get("inviteMobile").toString());
        				aRel.setInviteUserId(Long.parseLong(rrse.get("userId").toString()));
        				aRel.setInviteMobile(mobile2);
        				aRel.setIsNew(new Byte("0"));//被邀请人是老用户
        				aRel.setCreateDate(new Date());
        				aRel.setUpdateDate(new Date());
        				awardBindRelService.insertAwardBindRel(aRel);
    				}else if(abrel==1){
    					return Response.fail("校验失败，您在当前活动中已与其他用户绑定过关系!");
    				}
    				respMap.put("isAppUser", "old");
    			}else if("new".equals(falge)){
    				respMap.put("isAppUser", "new");
    			}
    			return Response.success("校验成功！", respMap);
	        		}
	        return Response.fail("校验失败,请重新注册！");
		} catch (Exception e) {
			logger.error("校验失败,请重新注册", e);
			return Response.fail("校验失败,请重新注册!");
		}
	}
	/**
	 * <pre>
	 * 3.输入密码注册新用户
	 * &#64;param mobile
	 * &#64;param randomCode
	 * </pre>
	 */
	@RequestMapping(value = "/new",method = RequestMethod.POST)
	public Response regsitNew(@RequestBody Map<String, Object> paramMap) {
		String mobile =   CommonUtils.getValue(paramMap, "mobile");// 手机号
		String password =  CommonUtils.getValue(paramMap, "password");//密码
		String InviterId=   CommonUtils.getValue(paramMap, "InviterId");//邀请人的id
		String mobile2=mobile.replace(" ", "");

		Pattern p = Pattern.compile("^1[0-9]{10}$");
		Matcher m = p.matcher(mobile2);
		Pattern pas = Pattern.compile("^[\\w]{6,12}$");
		Matcher mas = pas.matcher(password);
		Pattern pas2 = Pattern.compile("^[A-Za-z]*$");
		Matcher mas2 = pas2.matcher(password);
		Pattern pas3 = Pattern.compile("^[0-9]*$");
		Matcher mas3 = pas3.matcher(password);
		if (StringUtils.isBlank( mobile2)) {
			return Response.fail("手机号不能为空");
		}else if(!m.matches()){
			return Response.fail("手机号格式不正确,请重新输入！");
		}else if (StringUtils.isAnyBlank(password)) {
			return Response.fail("密码不能为空");
		}else if (password.length()<6){
			return Response.fail("请输入6-12位字母与数字的组合");
		}else if (password.length()>12){
			return Response.fail("请输入6-12位字母与数字的组合");
		}else if (mas2.matches()){
			return Response.fail("请输入6-12位字母与数字的组合！");
		}else if (mas3.matches()){
			return Response.fail("请输入6-12位字母与数字的组合！");
		}else if (!mas.matches()){
			return Response.fail("请输入6-12位字母与数字的组合！");
		}else if (StringUtils.isAnyBlank(InviterId)) {
			return Response.fail("邀请人的id不能为空");
		}
		try {
			   Response resp=registerInfoService.regsitNew(mobile2,password,InviterId);
	        	if("1".equals(resp.getStatus())){
	        		ActivityName activityName=ActivityName.INTRO;//获取活动名称
 			        AwardActivityInfoVo aInfoVo=awardActivityInfoService.getActivityByName(activityName);
	 			   	if(null==aInfoVo){
						return Response.fail("绑定关系失败,无有效活动！");
					}
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					Date aStartDate=sdf.parse(aInfoVo.getaStartDate());
					Date nowtime=new Date();
					int result=nowtime.compareTo(aStartDate);
					if(result<0){
						return Response.fail("绑定关系失败,活动还未开始！");
					}
    				AwardBindRel abr=new AwardBindRel();
    				abr.setInviteMobile(mobile2);
    				abr.setActivityId(aInfoVo.getId());
    				Integer abrel=awardBindRelService.selectByMobileAndActivityId(abr);//判断是否已经被邀请
    				if(abrel==0){
    					    Map<String,Object> rrse=(Map<String, Object>) resp.getData();
    	        			AwardBindRel aRel=new AwardBindRel();
    	    				aRel.setActivityId(aInfoVo.getId());
    	    				aRel.setUserId(Long.parseLong(InviterId));
    	    				aRel.setMobile(rrse.get("inviteMobile").toString());
    	    				aRel.setInviteUserId(Long.parseLong(rrse.get("userId").toString()));
    	    				aRel.setInviteMobile(mobile2);
    	    				aRel.setIsNew(new Byte("1"));//被邀请人是新用户
    	    				aRel.setCreateDate(new Date());
    	    				aRel.setUpdateDate(new Date());
    	    				awardBindRelService.insertAwardBindRel(aRel);
    				}else if(abrel==1){
    					return Response.fail("校验失败，您在当前活动中已与其他用户绑定过关系!");
    				}
	        	return Response.success("注册成功！");
	        }
	        return Response.fail("注册新用户失败");
		} catch (Exception e) {
			logger.error("注册新用户失败！", e);
			return Response.fail("注册新用户失败");
		}
	}
	
	/**
	 * <pre>
	 * 3.根据邀请人的id获取邀请人的信息（姓名，身份证号，签名）
	 * &#64;param mobile
	 * &#64;param randomCode
	 * </pre>
	 */
	@RequestMapping(value = "/inviterInfo",method = RequestMethod.POST)
	public Response getInviterInfo(@RequestBody Map<String, Object> paramMap) {
		String InviterId=   CommonUtils.getValue(paramMap, "InviterId");//邀请人的id
		if (StringUtils.isBlank( InviterId)) {
			return Response.fail("邀请人的id不能为空");
		}
		try {
			 Response resp =registerInfoService.getInviterInfo(InviterId);
			 if("1".equals(resp.getStatus())){
				 Map<String,Object> rrse=(Map<String, Object>) resp.getData();
				 return Response.success("获取邀请人的信息成功！", rrse);
			 }
			 return Response.fail("获取邀请人的信息失败！");
		} catch (Exception e) {
			logger.error("获取邀请人的信息失败！", e);
			return Response.fail("获取邀请人的信息失败!");
		}
	}
	
	/**
	 * 获取有效活动开始时间
	 */
	@RequestMapping(value = "/activityTime",method = RequestMethod.POST)
	public Response getActivityTIme(@RequestBody Map<String, Object> paramMap) {
		ActivityName activityName=ActivityName.INTRO;//获取活动名称
	        try {
				AwardActivityInfoVo aInfoVo=awardActivityInfoService.getActivityByName(activityName);
				String aStartTime=aInfoVo.getaStartDate();
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
				long startTime=sdf.parse(aStartTime).getTime();
				Map<String,Object> result=new HashMap<String,Object>();
				result.put("startTime", startTime);
				return Response.success("获取有效活动开始时间成功！",result);
			} catch (BusinessException e) {
				logger.error("获取有效活动开始时间失败！", e);
				return Response.fail("活动未开始!");
			}catch (ParseException e) {
				logger.error("获取有效活动开始时间失败！", e);
				return Response.fail("活动未开始!");
			}
	}
	
}
