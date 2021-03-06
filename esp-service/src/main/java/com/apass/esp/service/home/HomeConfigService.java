package com.apass.esp.service.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apass.esp.common.model.QueryParams;
import com.apass.esp.domain.dto.goods.HomeConfigDto;
import com.apass.esp.domain.entity.HomeConfig;
import com.apass.esp.domain.enums.YesNoEnums;
import com.apass.esp.domain.vo.HomeConfigVo;
import com.apass.esp.mapper.HomeConfigMapper;
import com.apass.esp.service.common.ImageService;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.BaseConstants;
import com.apass.gfb.framework.utils.DateFormatUtil;

@Service
public class HomeConfigService {

	@Autowired
	private HomeConfigMapper configMapper;
	
	@Autowired
	private ImageService imageService;
	/**
	 * 获取反馈信息列表
	 * @param query
	 * @return
	 * @throws BusinessException 
	 */
	public ResponsePageBody<HomeConfigVo> getHomeConfigListPage(QueryParams query) throws BusinessException{
		ResponsePageBody<HomeConfigVo> pageBody = new ResponsePageBody<HomeConfigVo>();
		List<HomeConfig> configList = configMapper.getHomeConfigListPage(query);
		Integer count = configMapper.getHomeConfigListPageCount();
		
		List<HomeConfigVo> configVoList = getPoToVoList(configList);
		
		pageBody.setTotal(count);
		pageBody.setRows(configVoList);
		pageBody.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
		return pageBody;
	}
	
	public List<HomeConfigVo> getPoToVoList(List<HomeConfig> configList){
		List<HomeConfigVo> configVoList = new ArrayList<HomeConfigVo>();
		for (HomeConfig config : configList) {
			configVoList.add(homeConfigPoToVo(config));
		}
		return configVoList;
	}
	
	public HomeConfigVo homeConfigPoToVo(HomeConfig config){
		HomeConfigVo vo = new HomeConfigVo();
		vo.setId(config.getId());
		vo.setActiveLink(config.getActiveLink());
		vo.setCreateDate(DateFormatUtil.dateToString(config.getCreateDate(), ""));
		vo.setEndTime(DateFormatUtil.dateToString(config.getEndTime(), ""));
		vo.setHomeName(config.getHomeName());
		vo.setHomeStatus(YesNoEnums.getMessage(config.getHomeStatus()));
		vo.setLogoUrl(config.getLogoUrl());
		vo.setStartTime(DateFormatUtil.dateToString(config.getStartTime(), ""));
		vo.setUpdateDate(DateFormatUtil.dateToString(config.getUpdateDate(), ""));
		return vo;
	}
	
	@Transactional(rollbackFor = { Exception.class})
	public Integer insert(HomeConfigDto configDto) {
		HomeConfig config = homeConfigDtoToPo(configDto,true);
		return configMapper.insertSelective(config);
	}
	
	public HomeConfig homeConfigDtoToPo(HomeConfigDto dto,boolean bl){
		HomeConfig config = new HomeConfig();
		config.setActiveLink(dto.getActiveLink());
		if(bl){
			config.setCreateDate(new Date());
		}
		config.setEndTime(DateFormatUtil.string2date(dto.getEndTime(), ""));
		config.setHomeName(dto.getHomeName());
		config.setHomeStatus(dto.getHomeStatus());
		config.setLogoUrl(dto.getLogoUrl());
		config.setStartTime(DateFormatUtil.string2date(dto.getStartTime(), ""));
		config.setUpdateDate(new Date());
		config.setId(dto.getId());
		return config;
	}
	
	@Transactional(rollbackFor = { Exception.class})
	public Integer update(HomeConfigDto configDto){
		HomeConfig config = homeConfigDtoToPo(configDto,false);
		return configMapper.updateByPrimaryKeySelective(config);
	}
	
	public List<HomeConfig> getContainsTimesList(String time,Long id){
		return configMapper.getContainsTimesList(time,id);
	}
	
	public HomeConfigVo getActiveConfig(String time,Long id) throws BusinessException{
		List<HomeConfig> configList = getContainsTimesList(time,id);
		HomeConfigVo vo = null;
		if(CollectionUtils.isNotEmpty(configList)){
			HomeConfig config = configList.get(0);
			vo = new HomeConfigVo();
			vo.setActiveLink(config.getActiveLink());
			vo.setEndTime(DateFormatUtil.dateToString(config.getEndTime(), ""));
			vo.setHomeName(config.getHomeName());
			vo.setId(config.getId());
			vo.setStartTime(DateFormatUtil.dateToString(config.getStartTime(), ""));
			vo.setLogoUrl(imageService.getImageUrl(config.getLogoUrl()));
			vo.setHomeStatus(config.getHomeStatus());
		}
		return vo;
	}
	
	public Integer getContainsTimesCount(String time,Long id){
		List<HomeConfig> list = getContainsTimesList(time,id);
		return CollectionUtils.isNotEmpty(list) ? list.size() : 0 ;
	}
	
	public Integer getContainsTimeCount(String startTime,String endTime,Long id){
		return configMapper.getContainsTimeCount(startTime, endTime,id);
	}
}
