package com.apass.esp.web.offer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.apass.esp.domain.entity.ProActivityCfg;
import com.apass.esp.domain.entity.jd.JdSellPrice;
import com.apass.esp.mapper.JdGoodsMapper;
import com.apass.esp.service.goods.GoodsStockInfoService;
import com.apass.esp.service.offer.ActivityCfgService;
import com.apass.esp.third.party.jd.entity.base.JdGoods;
import com.apass.gfb.framework.utils.GsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.entity.ProGroupGoods;
import com.apass.esp.domain.entity.ProGroupManager;
import com.apass.esp.domain.entity.goods.GoodsBasicInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsStockInfoEntity;
import com.apass.esp.domain.enums.SourceType;
import com.apass.esp.domain.query.ProGroupGoodsQuery;
import com.apass.esp.domain.vo.GoodsOrderSortVo;
import com.apass.esp.domain.vo.GroupManagerVo;
import com.apass.esp.domain.vo.GroupVo;
import com.apass.esp.domain.vo.ProGroupGoodsTo;
import com.apass.esp.domain.vo.ProGroupGoodsVo;
import com.apass.esp.mapper.ProGroupManagerMapper;
import com.apass.esp.repository.goods.GoodsStockInfoRepository;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.jd.JdGoodsInfoService;
import com.apass.esp.service.offer.GroupManagerService;
import com.apass.esp.service.offer.ProGroupGoodsService;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.esp.utils.ValidateUtils;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.log.LogAnnotion;
import com.apass.gfb.framework.log.LogValueTypeEnum;
import com.apass.gfb.framework.security.toolkit.SpringSecurityUtils;
import com.apass.gfb.framework.utils.BaseConstants.CommonCode;

/**
 * 导入.xlsx .xls文件
 * 
 * @author zengqingshan
 *
 */
@Controller
@RequestMapping("/application/activity")
public class ProGroupGoodsExportFikeController {
	/**
	 * 日志
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ProGroupGoodsExportFikeController.class);
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private ProGroupGoodsService proGroupGoodsService;
	@Autowired
	private GroupManagerService groupManagerService;
	@Autowired
	private ProGroupManagerMapper groupManagerMapper;
	@Autowired
	private JdGoodsInfoService jdGoodsInfoService;
    @Autowired
    private GoodsStockInfoRepository goodsStockInfoRepository;

    @Autowired
	private ActivityCfgService activityCfgService;

	@Autowired
	private GoodsStockInfoService goodsStockInfoService;
	@Autowired
	private JdGoodsMapper jdGoodsMapper;

	/**
     * 商品池分页json
     */
    @ResponseBody
    @RequestMapping(value ="/list")
    public ResponsePageBody<ProGroupGoodsVo> ProGroupGoodsPageList(ProGroupGoodsQuery query) {
    	ResponsePageBody<ProGroupGoodsVo> respBody = new ResponsePageBody<ProGroupGoodsVo>();
		try {
			String goodsCategory=query.getGoodsCategory();
			   if (StringUtils.isNotBlank(goodsCategory)) {
	                String[] aArray = goodsCategory.split("_");
	                String level = aArray[0];
	                String id = aArray[1];
	                if ("1".equals(level)) {
	                    if (!("-1".equals(id))) {
	                    	query.setCategoryId1(Long.valueOf(id));
	                    }
	                } else if ("2".equals(level)) {
	                	query.setCategoryId2(Long.valueOf(id));
	                } else if ("3".equals(level)) {
	                	query.setCategoryId3(Long.valueOf(id));
	                }
	            }
			ResponsePageBody<ProGroupGoodsVo> pagination= proGroupGoodsService.getProGroupGoodsListPage(query);
            respBody.setTotal(pagination.getTotal());
            respBody.setRows(pagination.getRows());
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
        	LOG.error("商品池查询失败!",e);
            respBody.setMsg("商品池查询失败");
        }
        return respBody;
    }
    /**
     * 加载活动分组
     */
    @ResponseBody
    @RequestMapping(value ="/loalgroupIds")
    public List<GroupVo>  ProGroupGoodsPageList(@RequestParam("activityId") String activityId) {
    	List<GroupManagerVo> result=groupManagerService.getGroupByActivityId(activityId);
    	List<GroupVo> list=new ArrayList<>();
    	if(null !=result && result.size()>0){
    		list=GroupManagerToVo(result);
    	}
    	return list;
    }
    
    /**
     * 商品的上移和下移
     */
	@ResponseBody
    @RequestMapping(value ="/edit/sort/save",method = RequestMethod.POST)
	@LogAnnotion(operationType = "分组商品上移下移", valueType = LogValueTypeEnum.VALUE_DTO)
	public Response groupEditSortSave(GoodsOrderSortVo vo){
		ProGroupGoods proGroupGoods = null;
		try {
			validateEditSortParams(vo);
			vo.setUserName(SpringSecurityUtils.getLoginUserDetails().getUsername());
			Integer i = proGroupGoodsService.editSortGroup(vo);
			if(i==1){
				proGroupGoods = proGroupGoodsService.selectByPrimaryKey(vo.getSubjectId());
				return Response.success("修改成功!",proGroupGoods);
			}
		}catch (BusinessException e) {
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			LOG.error("修改分组排序信息失败", e);
		}
		return Response.fail("修改分组排序信息失败");
	}
    
	/**
	 * 添加商品到分组中
	 */
	@ResponseBody
	@RequestMapping(value = "/addOneGoods")
	@LogAnnotion(operationType = "商品添加至分组", valueType = LogValueTypeEnum.VALUE_DTO)
	public Response ProGroupGoodsPageList(@RequestParam("activityId") String activityId,
			@RequestParam("groupNameId") String groupNameId, @RequestParam("goodsId") String goodsId, @RequestParam("skuId") String skuId) {
		int count = 0;
		int countSuccess = 0;
		int countFail = 0;
		if (StringUtils.isEmpty(goodsId) || StringUtils.isEmpty(skuId)) {
			return Response.fail("请选择商品！");
		}
		try {
			String[] goods = goodsId.split(",");
			String[] skuIds = skuId.split(",");
			count = goods.length;
			for (int i = 0; i < goods.length; i++) {
				ProGroupGoods proGroupGoods = proGroupGoodsService
						.selectOneBySkuIdAndActivityId(skuIds[i], Long.parseLong(activityId));
				if (null != proGroupGoods && StringUtils.equals(proGroupGoods.getStatus(), "S")
						&& StringUtils.equals(proGroupGoods.getGroupId() + "", groupNameId)) {
					countFail++;
					continue;
				}
				if (null != proGroupGoods && !proGroupGoods.getGroupId().equals(groupNameId)) {
					if (proGroupGoods.getStatus().equals("S")) {
						if (count > 1) {
							return Response.fail("其中有些商品已添加至其他分组！");
						} else {
							return Response.fail("该商品已添加至其他分组！");
						}
					}
					int groupSortId = proGroupGoodsService.getMaxSortOrder(Long.parseLong(groupNameId));
					proGroupGoods.setOrderSort(Long.parseLong(groupSortId + ""));
					proGroupGoods.setGroupId(Long.parseLong(groupNameId));
					proGroupGoods.setStatus("S");
					proGroupGoods.setSimilarFlag("1");
					proGroupGoods.setUpdatedTime(new Date());
					proGroupGoodsService.updateProGroupGoods(proGroupGoods);
					countSuccess++;
					TreeSet<String>  similarSkuIds = new TreeSet<>();
					GoodsInfoEntity goodsInfoEntity=goodsService.getGoodsInfo(skuIds[i]);
					//获取相似同类的商品的skuId
					if(StringUtils.equals(goodsInfoEntity.getSource(), SourceType.WZ.getCode())){
						similarSkuIds=jdGoodsInfoService.getJdSimilarSkuIdList(skuIds[i]);
					}else{
			            List<GoodsStockInfoEntity> jdGoodsStockInfoList = goodsStockInfoRepository.loadByGoodsId(goodsInfoEntity.getId());
			            for (GoodsStockInfoEntity goodsStockInfoEntity : jdGoodsStockInfoList) {
			            	similarSkuIds.add(goodsStockInfoEntity.getSkuId());
						}
					}
				   	if(CollectionUtils.isNotEmpty(similarSkuIds)){
				   		similarSkuIds.remove(skuIds[i]);
                	}
					for (String string : similarSkuIds) {
						ProGroupGoods proGroupGoods2 = proGroupGoodsService
								.selectOneBySkuIdAndActivityId(string, Long.parseLong(activityId));
						if(null != proGroupGoods2){
							int groupSortId2 = proGroupGoodsService.getMaxSortOrder(Long.parseLong(groupNameId));
							proGroupGoods2.setOrderSort(Long.parseLong(groupSortId2 + ""));
							proGroupGoods2.setGroupId(Long.parseLong(groupNameId));
							proGroupGoods2.setStatus("S");
							proGroupGoods2.setSimilarFlag("0");//同类商品标识
							proGroupGoods2.setUpdatedTime(new Date());
							proGroupGoodsService.updateProGroupGoods(proGroupGoods2);
							countSuccess++;
						}
					}
				} else {
					countFail++;
				}
			}
			// 更新分组中商品的总个数
			ProGroupManager group = groupManagerMapper.selectByPrimaryKey(Long.parseLong(groupNameId));
			if (null != group && countSuccess>0) {
				group.setGoodsSum(group.getGoodsSum() + countSuccess);
				group.setUpdatedTime(new Date());
				group.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
				groupManagerMapper.updateByPrimaryKey(group);
			}

		} catch (Exception e) {
			LOG.error("添加至该活动失败！", e);
			Response.fail("添加至该活动失败！");
		}
		return Response.success("共" + count + "件商品，关联成功" + countSuccess + "件，失败" + countFail + "件");
	}
	/**
	 *从分组中移除该商品(恢复到导入时的状态)
	 */
	@ResponseBody
	@RequestMapping(value = "/removeGoods")
	@LogAnnotion(operationType = "从分组移除商品", valueType = LogValueTypeEnum.VALUE_DTO)
	public Response RemoveGoodsFromGroup(@RequestParam("id") String id) {
		ProGroupManager proGroupManager = null;
		try {
			if(StringUtils.isBlank(id)){
				return Response.fail("移除失败！");
			}
			ProGroupGoods proGroupGoods=new ProGroupGoods();
			proGroupGoods.setId(Long.parseLong(id));
			proGroupGoods.setGroupId(-1L);
			proGroupGoods.setOrderSort(Long.parseLong("1"));
			proGroupGoods.setStatus("");
			proGroupGoods.setUpdatedTime(new Date());
			proGroupGoods.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
			
			ProGroupGoods entity = proGroupGoodsService.selectByPrimaryKey(Long.valueOf(id));
			int count  = proGroupGoodsService.updateGoods(proGroupGoods);
			//删除与之相似的且添加同一分组的商品
			//获取相似同类的商品的skuId
			GoodsInfoEntity goodsInfoEntity=goodsService.getGoodsInfo(entity.getSkuId());
			TreeSet<String>  similarSkuIds = new TreeSet<>();
			if(StringUtils.equals(goodsInfoEntity.getSource(), SourceType.WZ.getCode())){
				similarSkuIds=jdGoodsInfoService.getJdSimilarSkuIdList(entity.getSkuId());
			}else{
	            List<GoodsStockInfoEntity> jdGoodsStockInfoList = goodsStockInfoRepository.loadByGoodsId(goodsInfoEntity.getId());
	            for (GoodsStockInfoEntity goodsStockInfoEntity : jdGoodsStockInfoList) {
	            	similarSkuIds.add(goodsStockInfoEntity.getSkuId());
				}
			}
			
		 	if(CollectionUtils.isNotEmpty(similarSkuIds)){
		 		similarSkuIds.remove(entity.getSkuId());
        	}
			int countNum=0;
			for (String string : similarSkuIds) {
				ProGroupGoods proGroupGoods2 = proGroupGoodsService
						.selectOneBySkuIdAndActivityId(string,entity.getActivityId());
				if(null !=proGroupGoods2 && StringUtils.equals(proGroupGoods2.getStatus(), "S")){
					ProGroupGoods proGroupGoods3=new ProGroupGoods();
					proGroupGoods3.setId(proGroupGoods2.getId());
					proGroupGoods3.setGroupId(-1L);
					proGroupGoods3.setOrderSort(Long.parseLong("1"));
					proGroupGoods3.setStatus("");
					proGroupGoods3.setUpdatedTime(new Date());
					proGroupGoods3.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
					int countSimimar  = proGroupGoodsService.updateGoods(proGroupGoods3);
					if(countSimimar==1){
						countNum++;
					}
				}
			}
			if(count == 1){
				Long groupId = entity.getGroupId();
				proGroupManager = groupManagerService.selectByPrimaryKey(groupId);
				long goodsSumNew = proGroupManager.getGoodsSum() - 1-countNum;
				proGroupManager.setGoodsSum(goodsSumNew);
				groupManagerService.updateByPrimaryKeySelective(proGroupManager);
			}
		} catch (Exception e) {
			LOG.error("移除失败！--Exception--", e);
			return Response.fail("移除失败！");
		}
		return Response.success("移除成功！",proGroupManager);
	}
	/**
	 * 导入文件
	 * 表有三列：商品编号/skuid,市场价,活动价
	 * 导入条件：不存在存在其他有效的活动中
	 * @param file
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/importFile")
	@LogAnnotion(operationType = "导入商品到活动", valueType = LogValueTypeEnum.VALUE_DTO)
	public Response ProGroupGoodsImportFile(@RequestParam("file") MultipartFile file,@RequestParam("activityId") String activityId) {
		InputStream importFilein = null;
		int count = 0;// 导入条数
		int countSuccess = 0;// 导入成功条数
		int countFail=0;//导入失败条数
		int countExit=0;//已经存在其他有效活动中的商品条数
		try {
			importFilein = file.getInputStream();
			String fileName = file.getOriginalFilename();
			String[] fileStrings = fileName.split("\\.");
			String type = fileStrings[fileStrings.length - 1];
			if (!type.equals("xlsx") && !type.equals("xls")) {
				return Response.fail("导入文件类型不正确。");
			}

			List<ProGroupGoodsTo> list = readImportExcel(importFilein);
			LOG.info("从表中读取的数据:{}",GsonUtils.toJson(list));

			count=list.size();
			if(null !=list){
				ProActivityCfg activityCfg = activityCfgService.getById(Long.valueOf(activityId));

				for(int i=0;i<list.size();i++){
					ProGroupGoods pggds=new ProGroupGoods();
					pggds.setCreateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());// 创建人
					pggds.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
					pggds.setCreatedTime(new Date());
					pggds.setUpdatedTime(new Date());
					BigDecimal zero=BigDecimal.ZERO;
					BigDecimal marketPrice= BigDecimal.ZERO;
					BigDecimal activityPrice = BigDecimal.ZERO;


					//判断该商品是否符合导入条件
					String id=list.get(i).getId();
					GoodsBasicInfoEntity gbity=null;
					Pattern pattern = Pattern.compile("[0-9]*");   
					Matcher isNum = pattern.matcher(id);  
					if(null !=id && isNum.matches()){
						gbity=goodsService.getByGoodsBySkuId(id);
					}
					LOG.info("skueId:{}对应商品信息:{}",id,GsonUtils.toJson(gbity));
					if (null !=id && null != gbity ) {
						//判断该商品是否存在其他有效的活动中

						Boolean result=proGroupGoodsService.selectEffectiveGoodsBySkuId(id);
						Boolean limitResult=proGroupGoodsService.getStatusBySkuId(activityId,id);
						LOG.info("{}商品是否参加有效活动;{}，是否参加限时购活动:{}",id,result?"否":"是",result?"否":"是");
						if(activityCfg.getActivityCate().intValue() == 1){
							//房易贷专属用户活动
							//活动价=京东价*n%
							List<String> wzGoodsIdList = new ArrayList<>();
							wzGoodsIdList.add(id);
							List<JdSellPrice> jdSellPrices = jdGoodsInfoService.getJdSellPriceBySku(wzGoodsIdList);
							LOG.info("{}返回的京东价格是:{}",id,GsonUtils.toJson(jdSellPrices.get(0)));
							if(CollectionUtils.isEmpty(jdSellPrices)){
								LOG.info("{}商品导入失败，失败原因是返回京东价为空",id);
								countFail++;
								continue;
							}
							BigDecimal jdPrice = jdSellPrices.get(0).getJdPrice();
							BigDecimal wzPrice = jdSellPrices.get(0).getPrice();
							activityPrice  = jdPrice.multiply(activityCfg.getFydActPer()).setScale(0, BigDecimal.ROUND_HALF_UP);
							marketPrice = jdPrice;

							//顺便更新下数据库里goods_stock 表价格字段
							//更新价格
							GoodsInfoEntity goodsInfoEntity = goodsService.selectGoodsByExternalId(String.valueOf(id));
							List<GoodsStockInfoEntity> goodsStockInfoEntityList = goodsService.loadDetailInfoByGoodsId(goodsInfoEntity.getId());
							if(CollectionUtils.isNotEmpty(goodsStockInfoEntityList)){
								GoodsStockInfoEntity goodsStockInfoEntity = goodsStockInfoEntityList.get(0);
								goodsStockInfoEntity.setGoodsCostPrice(wzPrice);
								goodsStockInfoEntity.setGoodsPrice(jdPrice);
								goodsStockInfoEntity.setMarketPrice(jdPrice);
								goodsStockInfoService.update(goodsStockInfoEntity);
							}



							//更新京东表
							JdGoods jdGoods = jdGoodsMapper.queryGoodsBySkuId(Long.valueOf(id));
							if (jdGoods != null) {
								JdGoods updateJdGoods = new JdGoods();
								updateJdGoods.setId(jdGoods.getId());
								updateJdGoods.setUpdateDate(new Date());
								updateJdGoods.setPrice(wzPrice);
								updateJdGoods.setJdPrice(jdPrice);
								jdGoodsMapper.updateByPrimaryKeySelective(updateJdGoods);
							}
						}else{

							if(list.get(i).getActivityPrice() != null){
								activityPrice=list.get(i).getActivityPrice().setScale(2, BigDecimal.ROUND_HALF_UP);
							}
							if(list.get(i).getMarketPrice() != null){
								marketPrice=list.get(i).getMarketPrice().setScale(2, BigDecimal.ROUND_HALF_UP);
							}
							LOG.info("市场价：{},活动价：{}",marketPrice.toString(),activityPrice.toString());
							if( null == activityPrice || activityPrice.compareTo(zero)<=0){
								LOG.info("{}商品导入失败，失败原因是活动价为空或活动价小于0",id);
								GoodsInfoEntity goods = goodsService.getGoodsInfo(id);
								pggds.setGoodsId(null != goods ? goods.getId() : -1L );
								pggds.setSkuId(id);
								pggds.setGoodsCode(null != goods ? goods.getGoodsCode() : "" );
								pggds.setMarketPrice(list.get(i).getMarketPrice());
								pggds.setActivityPrice(list.get(i).getActivityPrice());
								pggds.setDetailDesc("0");//0表示导入失败
								pggds.setActivityId(Long.parseLong(activityId));
								proGroupGoodsService.insertSelective(pggds);
								countFail++;
								continue;
							}
						}


						if (result && limitResult) {//允许导入
							pggds.setMarketPrice(marketPrice);//对小数点第三位执行四舍五入
							pggds.setActivityPrice(activityPrice);
							pggds.setGoodsId(gbity.getGoodId());
							pggds.setSkuId(id);
							pggds.setGoodsCode(gbity.getGoodsCodeString());
							pggds.setDetailDesc("1");// 1表示导入成功
							pggds.setActivityId(Long.parseLong(activityId));
							proGroupGoodsService.insertSelective(pggds);
						    countSuccess++;

						}else{//该商品在其他有效活动中，导入失败
							LOG.info("{}导入失败，失败原因是该商品在其他有效活动中",id);
							pggds.setGoodsId(gbity.getGoodId());
							pggds.setSkuId(id);
							if(gbity.getGoodsCode() != null){
								pggds.setGoodsCode(gbity.getGoodsCode().toString());
							}
							pggds.setMarketPrice(marketPrice);
							pggds.setActivityPrice(activityPrice);
							pggds.setDetailDesc("0");//0表示导入失败
							pggds.setActivityId(Long.parseLong(activityId));
							proGroupGoodsService.insertSelective(pggds);
							countExit++;
						}
					}else{
						LOG.info("{}导入失败，失败原因是skuId为空或未查到商品",id);
						GoodsInfoEntity goods = goodsService.getGoodsInfo(id);
						pggds.setGoodsId(null != goods ? goods.getId() : -1L );
						pggds.setSkuId(id);
						pggds.setGoodsCode(null != goods ? goods.getGoodsCode() : "" );
						pggds.setMarketPrice(list.get(i).getMarketPrice());
						pggds.setActivityPrice(list.get(i).getActivityPrice());
						pggds.setDetailDesc("0");//0表示导入失败
						pggds.setActivityId(Long.parseLong(activityId));
						proGroupGoodsService.insertSelective(pggds);
						countFail++;
					}
				}
			}
		} catch (Exception e) {
			 LOG.error("服务器忙，请稍后再试。", e);
	         return Response.fail(e.getMessage());
		}
		return Response.success("本次共导入"+count+"件商品，导入成功"+countSuccess+"件，已存在其他有效活动中"+countExit+"件，导入失败"+countFail+"件");
	}
	// 将上传文件读取到List中
	private List<ProGroupGoodsTo> readImportExcel(InputStream in) throws IOException, InvalidFormatException {
		Workbook hssfWorkbook = WorkbookFactory.create(in); 
		//hssfWorkbook = new HSSFWorkbook(in);
		List<ProGroupGoodsTo> list = new ArrayList<ProGroupGoodsTo>();
		// 获取第一页（sheet）
		Sheet hssfSheet = hssfWorkbook.getSheetAt(0);
		// 第一行为标题，数据从第二行开始获取
		// 得到总行数
		int rowNum = hssfSheet.getLastRowNum() + 1;
		int count = 0;
  		for (int i = 1; i < rowNum; i++) {
			Row hssfRow = hssfSheet.getRow(i);
			if(count == 3){
				break;
			}
			if (hssfRow == null) {
				count ++;
				continue;
			}
			count = 0;
			ProGroupGoodsTo pggt = new ProGroupGoodsTo();
			// 表格中共有3列（商品编号/skuid,市场价,活动价）
			for (int j = 0; j < 3; j++) {
				Cell cell = hssfRow.getCell(j);
				if (cell == null) {
					continue;
				}
				switch (j) {
				case 0:
					if (!StringUtils.isBlank(getValue(cell))) {
						pggt.setId(getValue(cell));
					}
					break;
				case 1:
					if (!StringUtils.isBlank(getValue(cell)) && ifBigDecimalString(getValue(cell))) {
						pggt.setMarketPrice(new BigDecimal(getValue(cell)));
					}
					break;
				case 2:
					if (!StringUtils.isBlank(getValue(cell)) && ifBigDecimalString(getValue(cell))) {
						pggt.setActivityPrice(new BigDecimal(getValue(cell)));
					}
					break;
				default:
					break;
				}
			}
			//如果商品编号不为空时，才认为是一条完整的数据
			if(StringUtils.isNotBlank(pggt.getId())){
				list.add(pggt);
			}
		}
		return list;
	}

	/**
	 * @param cell
	 * @return Excel中每一个格子中的值
	 */
	private String getValue(Cell cell) {
		String value = null;
		// 简单的查检列类型
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:// 字符串
			value = cell.getRichStringCellValue().getString();
			break;
		case Cell.CELL_TYPE_NUMERIC:// 数字
			BigDecimal big = new BigDecimal(cell.getNumericCellValue());
			value = big.toString();
			// 解决1234.0 去掉后面的.0
			if (null != value && !"".equals(value.trim())) {
				String[] item = value.split("[.]");
				if (1 < item.length && "0".equals(item[1])) {
					value = item[0];
				}
			}
			break;
		case Cell.CELL_TYPE_BLANK:
			value = "";
			break;
		case Cell.CELL_TYPE_FORMULA:
			value = String.valueOf(cell.getCellFormula());
			break;
		case Cell.CELL_TYPE_BOOLEAN:// boolean型值
			value = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_ERROR:
			value = String.valueOf(cell.getErrorCellValue());
			break;
		default:
			break;
		}
		return value;
	}

	/**
	 * 判断字符串是否是BigDecimal类型数字
	 * 
	 * @param str
	 * @return
	 */
	private boolean ifBigDecimalString(String str) {
		try {
			BigDecimal bigDecimal = new BigDecimal(str);// 把字符串强制转换为BigDecimal
			return true;// 如果是数字，返回True
		} catch (Exception e) {
			return false;// 如果抛出异常，返回False
		}
	}
	/**
     * 判断字符串是否是Long类型数字
     */
    private boolean ifLongString(String str) {
        try {
            long num = Long.valueOf(str);// 把字符串强制转换为数字
            return true;// 如果是数字，返回True
        } catch (Exception e) {
            return false;// 如果抛出异常，返回False
        }
    }
	/**
	 * 验证排序
	 * @param vo
	 * @throws BusinessException
	 */
	public void validateEditSortParams(GoodsOrderSortVo vo) throws BusinessException{
		ValidateUtils.isNullObject(vo.getSubjectId(), "主操作Id不能为空!");
		ValidateUtils.isNullObject(vo.getPassiveId(), "被操作Id不能为空!");
	}
	
	private List<GroupVo> GroupManagerToVo(List<GroupManagerVo> list){
		List<GroupVo>  groupList=new ArrayList<>();
		for(GroupManagerVo gv:list){
			GroupVo groupVo=new GroupVo();
			groupVo.setId(gv.getId().toString());
			groupVo.setText(gv.getGroupName());
			groupList.add(groupVo);
		}
		return groupList;
	}

	/**
	 * 创建分组
	 */
	@ResponseBody
	@RequestMapping(value ="/addGroup")
	public Response createGroup(ProGroupManager proGroupManager){
		if(StringUtils.isBlank(proGroupManager.getGroupName())){
			return Response.fail("分组名称不能为空");
		}

		if(proGroupManager.getOrderSort() == null){
			return Response.fail("排序不能为空");
		}
		String currentUser = SpringSecurityUtils.getCurrentUser();
		proGroupManager.setCreateUser(currentUser);
		proGroupManager.setUpdateUser(currentUser);
		proGroupManager.setCreatedTime(new Date());
		proGroupManager.setUpdatedTime(new Date());

		groupManagerService.addGroup(proGroupManager);

		return Response.success("创建分组成功！");
	}

}
