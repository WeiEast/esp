package com.apass.esp.service.offer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apass.esp.domain.entity.ProActivityCfg;
import com.apass.esp.domain.entity.ProGroupGoods;
import com.apass.esp.domain.entity.ProGroupManager;
import com.apass.esp.domain.query.GroupQuery;
import com.apass.esp.domain.vo.GroupGoodsVo;
import com.apass.esp.domain.vo.GroupManagerVo;
import com.apass.esp.domain.vo.ProCouponVo;
import com.apass.esp.mapper.ProActivityCfgMapper;
import com.apass.esp.mapper.ProGroupGoodsMapper;
import com.apass.esp.mapper.ProGroupManagerMapper;
import com.apass.esp.repository.banner.BannerInfoRepository;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.BaseConstants;
import com.google.common.collect.Maps;


@Service
public class GroupManagerService {
	
	private static final Logger logger = LoggerFactory.getLogger(GroupManagerService.class);

	@Autowired
	private ProGroupManagerMapper groupManagerMapper;
	
	@Autowired
	private ProGroupGoodsMapper groupGoodsMapper;
	
	@Autowired
	private ProActivityCfgMapper activityCfgMapper;
	
	@Autowired
	private ProGroupGoodsService proGroupGoodsService;
	
	@Autowired
	private BannerInfoRepository bannerMapper;
	
	@Autowired
	private CouponManagerService couponManagerService;
	/**
	 * 获取活动配置信息
	 * @param query
	 * @return
	 * @throws BusinessException 
	 */
	public ResponsePageBody<GroupManagerVo> getActivityGroupListPage(GroupQuery group) throws BusinessException{
		ResponsePageBody<GroupManagerVo> pageBody = new ResponsePageBody<GroupManagerVo>();
		List<ProGroupManager> groupList =  groupManagerMapper.getGroupByActIdListPage(group);
		Integer count = groupManagerMapper.getGroupByActIdListPageCount(group);
		
		List<GroupManagerVo> configVoList = getGroupManageVoList(groupList);
		
		pageBody.setTotal(count);
		pageBody.setRows(configVoList);
		pageBody.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
		return pageBody;
	}
	/**
	 * 根据活动配置的id，获取活动所属的分组
	 * @param activityId
	 * @return
	 * @throws BusinessException
	 */
	public List<GroupManagerVo> getGroupByActivityId(String activityId){
		List<ProGroupManager> groupList =  groupManagerMapper.getGroupByActivityId(Long.parseLong(activityId));
		return getGroupManageVoList(groupList);
	}
	
	/**
	 * 根据活动的id，获取下属分组和分组下的商品,如果分组下不存在商品则，不查询出来
	 * @param activityId
	 * @return
	 * @throws BusinessException 
	 */
	public Map<String,Object> getGroupsAndGoodsByActivityId(String activityId,String bannerId,String userId) throws BusinessException{
		
		Map<String,Object> maps = Maps.newHashMap();
		
		ProActivityCfg activity =  activityCfgMapper.selectByPrimaryKey(Long.parseLong(activityId));
		if(null == activity){
			throw new BusinessException("活动不存在");
		}
		
		Date currentTime = new Date();
		
		//活动的状态
		String status = "";
		if(activity.getEndTime().getTime() < currentTime.getTime()){
			//如果banner下配置的活动已结束，就删掉banner
			if(StringUtils.isNotBlank(bannerId)){
				bannerMapper.delete(Long.parseLong(bannerId));
			}
			status = "end";
		}
		List<GroupManagerVo> groupVoList = new ArrayList<GroupManagerVo>();
		if(StringUtils.isEmpty(status)){
			groupVoList = getGroupByActivityId(activityId);
			if(CollectionUtils.isNotEmpty(groupVoList)){
				for(int i = groupVoList.size() - 1;i >= 0;i--){
					GroupManagerVo vo = groupVoList.get(i);
					vo.setActivityName(activity.getActivityName());
					List<GroupGoodsVo> goodsList = proGroupGoodsService.getGroupGoodsByGroupId(vo.getId());
					if(CollectionUtils.isEmpty(goodsList)){
						groupVoList.remove(vo);
						continue;
					}
					vo.setGoodsList(goodsList);
				}
			}
		}
		
		/**
		 * sprint 11 根据活动的Id，获取对应优惠券的信息
		 */
		List<ProCouponVo> couponVos = couponManagerService.getCouponVos(userId,activityId);
		maps.put("coupons", couponVos);
		maps.put("groups", groupVoList);
		maps.put("status",status);
		return maps;
	}
	
	
	/**
	 * 保存新添加分组信息
	 * @param vo
	 * @return
	 * @throws BusinessException 
	 */
	@Transactional(rollbackFor = { Exception.class})
	public Integer saveGroup(GroupManagerVo vo,String userName) throws BusinessException{
		ProGroupManager manager = getProGroupManager(vo,true,userName);
		validateGroupName(vo, userName);
		updateGroupOrderSort(vo, userName);
		return groupManagerMapper.insertSelective(manager);
	}

	/**
	 * 验证分组分组名称是否重复和更新分组的排序字段
	 * @param vo
	 * @param userName
	 * @throws BusinessException
	 */
	public void validateGroupName(GroupManagerVo vo,String userName) throws BusinessException{
		/**
		 * 根据分组的名称查询，同一活动下是否存在相同的分组名称
		 */
		List<ProGroupManager> groupList = groupManagerMapper.getGroupByActiIdAndGroupName(new GroupQuery(vo.getActivityId(),null,vo.getGroupName()));
		if(CollectionUtils.isNotEmpty(groupList)){
			throw new BusinessException("该分组名称已经存在，请重新填写!");
		}
	}
	
	/**
	 * 批量更新分组的排序字段
	 * @param vo
	 * @param userName
	 */
	@Transactional(rollbackFor = { Exception.class})
	public void updateGroupOrderSort(GroupManagerVo vo,String userName){
		/**
		 * 首先根据活动的Id，查询出该活动下所有的分组信息
		 */
		List<ProGroupManager> groupSortList = groupManagerMapper.getGroupByActiIdAndOrderSort(new GroupQuery(vo.getActivityId(),vo.getId(),vo.getOrderSort()));
		if(CollectionUtils.isNotEmpty(groupSortList)){
			ProGroupManager manager = groupSortList.get(0);
			if(manager.getOrderSort().equals(vo.getOrderSort())){
				for (ProGroupManager group : groupSortList) {
					group.setOrderSort(group.getOrderSort() +1);
					group.setUpdatedTime(new Date());
					group.setUpdateUser(userName);
					groupManagerMapper.updateByPrimaryKeySelective(group);
				}
			}
		}
	}
	
	/**
	 * 创建分组
	 * @param proGroupManager
	 * @return
     */
	@Transactional(rollbackFor = { Exception.class})
	public Integer addGroup(ProGroupManager proGroupManager){
		return groupManagerMapper.insert(proGroupManager);
	}


	
	/**
	 * 保存修改分组信息
	 * @param vo
	 * @return
	 * @throws BusinessException 
	 */
	@Transactional(rollbackFor = { Exception.class})
	public Integer editGroup(GroupManagerVo vo,String userName) throws BusinessException{
		ProGroupManager manager = getProGroupManager(vo,false,userName);
		ProGroupManager exsit = groupManagerMapper.selectByPrimaryKey(vo.getId());
		if(!StringUtils.equals(vo.getGroupName(), exsit.getGroupName())){
			validateGroupName(vo, userName);
		}
		if(vo.getOrderSort().longValue() != exsit.getOrderSort().longValue()){
			updateGroupOrderSort(vo, userName);
		}
		return groupManagerMapper.updateByPrimaryKeySelective(manager);
	}
	
	@Transactional(rollbackFor = { Exception.class})
	public Integer deleteGroup(Long id) throws BusinessException{
		if(null == id){
			throw new BusinessException("分组编号不能为空!");
		}
		List<ProGroupGoods> goodsList = groupGoodsMapper.selectGoodsByGroupId(id);
		if(CollectionUtils.isNotEmpty(goodsList)){
			logger.error("分组编号为{}下存在关联商品，不能删除!",id);
			throw new BusinessException("请先将该分组下的商品删除后再次操作！");
		}
		return groupManagerMapper.deleteByPrimaryKey(id);
	}
	
	public List<GroupManagerVo> getGroupManageVoList(List<ProGroupManager> groupList){
		List<GroupManagerVo> voList = new ArrayList<GroupManagerVo>();
		for (ProGroupManager pro : groupList) {
			voList.add(getGroupManagerVo(pro));
		}
		return voList;
	}
	
	public GroupManagerVo getGroupManagerVo(ProGroupManager pro){
		GroupManagerVo vo = new GroupManagerVo();
		vo.setActivityId(pro.getActivityId());
		vo.setGoodsSum(pro.getGoodsSum());
		vo.setGroupName(pro.getGroupName());
		vo.setId(pro.getId());
		vo.setOrderSort(pro.getOrderSort());
		return vo;
	}
	
	public ProGroupManager getProGroupManager(GroupManagerVo vo,boolean bl,String userName){
		
		ProGroupManager group = new ProGroupManager();
		group.setGroupName(vo.getGroupName());
		group.setOrderSort(vo.getOrderSort());
		if(bl){
			group.setCreatedTime(new Date());
			group.setCreateUser(userName);
			group.setActivityId(vo.getActivityId());
			group.setGoodsSum(vo.getGoodsSum());
		}
		group.setUpdatedTime(new Date());
		group.setUpdateUser(userName);
		group.setId(vo.getId());
		return group;
	}

	public ProGroupManager selectByPrimaryKey(Long groupId) {
		return groupManagerMapper.selectByPrimaryKey(groupId);
	}

	public Integer updateByPrimaryKeySelective(ProGroupManager record) {
		return groupManagerMapper.updateByPrimaryKeySelective(record);
	}
}
