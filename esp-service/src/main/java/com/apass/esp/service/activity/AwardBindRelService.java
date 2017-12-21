package com.apass.esp.service.activity;


import java.util.*;
import java.util.Calendar;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.entity.AwardBindRel;
import com.apass.esp.domain.query.ActivityBindRelStatisticQuery;
import com.apass.esp.domain.vo.ActivityDetailStatisticsVo;
import com.apass.esp.mapper.AwardBindRelMapper;
import com.apass.esp.service.registerInfo.RegisterInfoService;
import com.apass.gfb.framework.utils.DateFormatUtil;

@Service
public class AwardBindRelService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AwardBindRelService.class);

	@Autowired
	public AwardBindRelMapper wihdrawBindRelMapper;
	@Autowired
	public RegisterInfoService registerInfoService;
	
    @Transactional(rollbackFor=Exception.class) 
	public int insertAwardBindRel(AwardBindRel awardBindRel){
    	int result2 = 0;
    	Long userId=awardBindRel.getUserId();
    	Long inviteUserId=awardBindRel.getInviteUserId();
    	Response result=registerInfoService.saveCustomerReferenceInfo(userId, inviteUserId);
    	if(result.statusResult()){
    		result2=wihdrawBindRelMapper.insert(awardBindRel);
    	}
		return result2;
	}
	
	public Integer selectCountByInviteMobile(String moblie){
		return wihdrawBindRelMapper.selectCountByInviteMobile(moblie);
	}

	public Integer selectByMobileAndActivityId(AwardBindRel abr){
		return wihdrawBindRelMapper.selectByMobileAndActivityId(abr);
	}
	
	public Integer selectByMobile(AwardBindRel abr){
		return wihdrawBindRelMapper.selectByMobile(abr);
	}

	public AwardBindRel getByInviterUserId(String userId,int activityId){
		return wihdrawBindRelMapper.getByInviterUserId(userId,activityId);

	}

	public List<AwardBindRel> selectByInviterUserId(String userId){
		return wihdrawBindRelMapper.selectByInviterUserId(userId);
	}
	
	public List<AwardBindRel> getAllByInviterUserId(String userId){
		return wihdrawBindRelMapper.getAllByInviterUserId(userId);
	}
	/**
	  * 统计查询在某一时间内邀请的总人数
	  */
	public Integer getInviterUserCountByTime(int days){
		ActivityBindRelStatisticQuery query = new ActivityBindRelStatisticQuery();
		//在当前时间的基础上减去days
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, days);
		query.setStartCreateDate(DateFormatUtil.dateToString(cal.getTime(),""));
		return wihdrawBindRelMapper.getInviterUserCountByTime(query);
	}
	/**
	 * 某段时间内某活动下的推荐人总数
	 */
	public  Integer refereeNums(ActivityBindRelStatisticQuery query){
		return wihdrawBindRelMapper.refereeNums(query);
	}

	/**
	 * 某段时间内某活动下的拉新人总数
	 */
	public Integer newNums(ActivityBindRelStatisticQuery query) {
		return wihdrawBindRelMapper.newNums(query);
	}

	/**
	 * 查询某段时间内某活动下的推荐人及推荐人拉新人数
	 */
	public List<ActivityDetailStatisticsVo> getUserIdListByActivityId(ActivityBindRelStatisticQuery query) {
		return wihdrawBindRelMapper.getUserIdListByActivityId(query);
	}

	public List<AwardBindRel> selectAllUserByCreateDate(String startCreateDate) {
		List<String> list = Lists.newArrayList();
		list.add("15073857658");
		list.add("18114470520");

		Map<String,Object> paramMap = Maps.newHashMap();
		paramMap.put("startCreateDate",startCreateDate);
		paramMap.put("mobiles",list);
		return wihdrawBindRelMapper.selectAllUserByCreateDate(paramMap);
	}
}
