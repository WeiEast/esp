package com.apass.esp.service.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.apass.esp.common.code.BusinessErrorCode;
import com.apass.esp.domain.dto.ProGroupGoodsBo;
import com.apass.esp.domain.dto.cart.GoodsIsSelectDto;
import com.apass.esp.domain.dto.cart.GoodsStockIdNumDto;
import com.apass.esp.domain.dto.cart.ListCartDto;
import com.apass.esp.domain.dto.goods.GoodsStockSkuDto;
import com.apass.esp.domain.entity.GoodsAttrVal;
import com.apass.esp.domain.entity.ProActivityCfg;
import com.apass.esp.domain.entity.ProGroupGoods;
import com.apass.esp.domain.entity.activity.LimitGoodsSkuVo;
import com.apass.esp.domain.entity.cart.CartInfoEntity;
import com.apass.esp.domain.entity.cart.GoodsInfoInCartEntity;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsStockInfoEntity;
import com.apass.esp.domain.entity.jd.JdSimilarSku;
import com.apass.esp.domain.entity.jd.JdSimilarSkuTo;
import com.apass.esp.domain.entity.jd.JdSimilarSkuVo;
import com.apass.esp.domain.enums.GoodStatus;
import com.apass.esp.domain.enums.JdGoodsImageType;
import com.apass.esp.domain.enums.SourceType;
import com.apass.esp.domain.enums.YesNo;
import com.apass.esp.mapper.ProGroupGoodsMapper;
import com.apass.esp.repository.cart.CartInfoRepository;
import com.apass.esp.repository.goods.GoodsRepository;
import com.apass.esp.repository.goods.GoodsStockInfoRepository;
import com.apass.esp.service.activity.LimitCommonService;
import com.apass.esp.service.common.CommonService;
import com.apass.esp.service.common.ImageService;
import com.apass.esp.service.goods.GoodsAttrValService;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.jd.JdGoodsInfoService;
import com.apass.esp.service.offer.ActivityCfgService;
import com.apass.esp.service.offer.ProGroupGoodsService;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.logstash.LOG;
import com.apass.gfb.framework.utils.EncodeUtils;

@Component
public class ShoppingCartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCartService.class);
    
    @Autowired
    private CartInfoRepository cartInfoRepository;
    
    @Autowired
    private GoodsRepository goodsInfoDao;
    
    @Autowired
    private GoodsStockInfoRepository goodsStockDao;
    
    @Autowired
    private CommonService commonService;

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private JdGoodsInfoService jdGoodsInfoService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private ProGroupGoodsService proGroupGoodsService;
    @Autowired
    private ProGroupGoodsMapper groupGoodsMapper;
    @Autowired
    private ActivityCfgService activityCfgService;
    @Autowired
    private GoodsAttrValService goodsAttrValService;
    @Autowired
    private LimitCommonService limitCommonService;
    /**
     * 添加商品到购物车
     * 
     * @param userId
     * @param goodsStockId
     * @param count
     * @throws BusinessException 
     */
    @Transactional(rollbackFor = Exception.class)
    public void addGoodsToCart(String requestId, String userId, String goodsStockId, String count) throws BusinessException {
        
        Long userIdVal = Long.valueOf(userId);
        Long goodsStockIdVal = Long.valueOf(goodsStockId);
        int countVal = Integer.parseInt(count);
        
        if(countVal < 1){
            LOG.info(requestId, "商品数量异常", String.valueOf(countVal));
            throw new BusinessException("购买商品不能少于1件", BusinessErrorCode.GOODS_ADDTOCART_ERROR);
        }
        
        // 查询商品库存信息(获取商品价格、当前库存量)
        GoodsStockInfoEntity goodsStockInfo = goodsStockDao.select(goodsStockIdVal);
        if(null == goodsStockInfo){
            LOG.info(requestId, "通过商品库存id查询商品库存信息为空", goodsStockId);
            throw new BusinessException("无效的商品id",BusinessErrorCode.GOODS_NOT_EXIST);
        }
        
        GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsStockInfo.getGoodsId());
        Date date = new Date();
    	if (null == goodsInfo || (null !=goodsInfo.getDelistTime() && goodsInfo.getDelistTime().before(date)) || goodsInfo.getIsDelete().equals("00")
				|| !GoodStatus.GOOD_UP.getCode().equals(goodsInfo.getStatus())) {
			LOG.info(requestId, "该商品已下架", goodsStockId);
			throw new BusinessException("该商品已下架", BusinessErrorCode.GOODS_ALREADY_REMOV);
		}
		if (!SourceType.WZ.getCode().equals(goodsInfo.getSource())){
			// 商品库存如果都为0 则提示商品下架
			List<GoodsStockInfoEntity> goodsList = goodsStockDao.loadByGoodsId(goodsStockInfo.getGoodsId());
			boolean offShelfFlag = true;
			for (GoodsStockInfoEntity goodsStock : goodsList) {
				if (goodsStock.getStockCurrAmt() > 0) {
					offShelfFlag = false;
					break;
				}
			}
			if (offShelfFlag) {
				LOG.info(requestId, "商品各规格数量都为0", goodsStockId);
				throw new BusinessException("该商品已下架", BusinessErrorCode.GOODS_ALREADY_REMOV);
			}

			if (goodsStockInfo.getStockCurrAmt() < 1 || goodsStockInfo.getStockCurrAmt() < countVal) {
				LOG.info(requestId, "该商品库存不足", goodsStockId);
				throw new BusinessException("该商品库存不足", BusinessErrorCode.GOODS_STOCK_NOTENOUGH);
			}
		}
        
        // 计算商品折扣后价格
        BigDecimal goodsPrice = commonService.calculateGoodsPrice(goodsStockInfo.getGoodsId(), goodsStockIdVal);

        // 获取该用户购物车中商品信息
        CartInfoEntity cartDto = new CartInfoEntity();
        cartDto.setUserId(userIdVal);
        List<CartInfoEntity> cartInfoList = cartInfoRepository.filter(cartDto);

        // 标记购物车中是否已存在该商品
        boolean goodsFlag = false;

        // 购物车已存在该商品，则增加数量
        if (null != cartInfoList && !cartInfoList.isEmpty()) {
            for (CartInfoEntity cartinfo : cartInfoList) {
                if (StringUtils.equals(String.valueOf(cartinfo.getGoodsStockId()), goodsStockId)) {
                    
                    int totalNum = cartinfo.getGoodsNum() + countVal;
                    /*
                    if(goodsStockInfo.getStockCurrAmt() < totalNum){
                        throw new BusinessException("该商品库存不足");
                    }
                    */
                    goodsFlag = true;
                    CartInfoEntity saveToCart = new CartInfoEntity();
                    saveToCart.setId(cartinfo.getId());
                    saveToCart.setGoodsSelectedPrice(goodsPrice);
                    saveToCart.setGoodsNum(totalNum);
                    saveToCart.setIsSelect("1");

                    Integer updateFlag = cartInfoRepository.update(saveToCart);
                    if(updateFlag != 1){
                        LOG.info(requestId, "购物车已存在该商品", "更新商品数量失败");
                        throw new BusinessException("添加商品到购物车失败",BusinessErrorCode.GOODS_ADDTOCART_ERROR);
                    }
                    break;
                }
            }
        }

        //  购物车不存该商品，则插入该商品信息
        if (!goodsFlag) {
            
            int numOfType = null == cartInfoList ? 0 : cartInfoList.size();
            if(numOfType >= 99){
                LOG.info(requestId, "购物车商品种类数已满", String.valueOf(numOfType));
                throw new BusinessException("您的购物车已满，快去结算吧!",BusinessErrorCode.CART_FULL);
            }
            
            CartInfoEntity saveToCart = new CartInfoEntity();
            saveToCart.setUserId(userIdVal);
            saveToCart.setGoodsStockId(goodsStockIdVal);
            saveToCart.setGoodsSelectedPrice(goodsPrice);
            saveToCart.setGoodsNum(countVal);
            saveToCart.setIsSelect("1");
            cartInfoRepository.insert(saveToCart);
            if(null == saveToCart.getId()){
                LOG.info(requestId, "购物车不存该商品", "插入商品信息失败");
                throw new BusinessException("添加商品到购物车失败",BusinessErrorCode.GOODS_ADDTOCART_ERROR);
            }
        }

    }

    /**
     * 修改商品数量
     * 
     * @param userId
     * @param goodsStockId
     * @param count
     * @throws BusinessException
     */
    @Transactional(rollbackFor = Exception.class)
    public void setGoodsAmount(String userId, String goodsStockId, String count) throws BusinessException {

        Long userIdVal = Long.valueOf(userId);
        Long goodsStockIdVal = Long.valueOf(goodsStockId);
        int countVal = Integer.parseInt(count);
        
        if(countVal < -1 || countVal == 0){
            LOGGER.error("修改商品数量错误[{}]", countVal);
            throw new BusinessException("修改商品数量错误");
        }
		// 查询商品库存信息
		GoodsStockInfoEntity goodsStockInfo = goodsStockDao.select(Long.parseLong(goodsStockId));
		if (null == goodsStockInfo) {
			throw new BusinessException("无效的商品id", BusinessErrorCode.GOODS_NOT_EXIST);
		}
		GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsStockInfo.getGoodsId());
		if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {
            //	京东商品购买数量不能超过200		
			if(countVal>200){
				LOGGER.error("商品数量不能超过200！", goodsStockId, countVal);
				throw new BusinessException("商品数量不能超过200");
			}
		}else{
			// 商品当前库存量
			int stockCurrAmt = goodsStockDao.getStockCurrAmt(goodsStockIdVal).intValue();

			if (stockCurrAmt < 1 || stockCurrAmt < countVal) {
				LOGGER.error("修改商品数量,商品库存ID[{}],当前库存量[{}],库存不足", goodsStockId, stockCurrAmt);
				throw new BusinessException("该商品库存不足");
			}
		}

        CartInfoEntity cartDto = new CartInfoEntity();
        cartDto.setUserId(userIdVal);
        cartDto.setGoodsStockId(goodsStockIdVal);

        List<CartInfoEntity> cartInfoList = cartInfoRepository.filter(cartDto);

        if (!cartInfoList.isEmpty()
            && StringUtils.equals(String.valueOf(cartInfoList.get(0).getGoodsStockId()), goodsStockId)) {

            CartInfoEntity saveToCart = new CartInfoEntity();
            saveToCart.setId(cartInfoList.get(0).getId());

            if (Math.abs(countVal) == 1) {
                saveToCart.setGoodsNum(cartInfoList.get(0).getGoodsNum() + countVal);
            } else {
                saveToCart.setGoodsNum(countVal);
            }

            cartInfoRepository.update(saveToCart);
        } else {
            LOGGER.error("购物车中不存在该商品,不能修改数量");
            throw new BusinessException("购物车中不存在该商品,不能修改数量");
        }
    }

    /**
     * 删除购物车中商品
     * 
     * @param userId
     * @param goodsStockIdArr
     * @throws BusinessException
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoodsInCart(String requestId, Long userId, String[] goodsStockIdArr) throws BusinessException {
        
        if(null == goodsStockIdArr || goodsStockIdArr.length == 0){
            LOG.info(requestId, "没有提交要删除的商品库存id", "");
            throw new BusinessException("请选择要删除的商品",BusinessErrorCode.GOODS_STOCKID_ERROR);
        }
        
        CartInfoEntity cartDto = new CartInfoEntity();
        cartDto.setUserId(userId);
        List<CartInfoEntity> cartInfoList = cartInfoRepository.filter(cartDto);
        if(null == cartInfoList || cartInfoList.isEmpty()){
            LOG.info(requestId, "查询该用户购物车商品信息", "该用户购物车为空");
            throw new BusinessException("该用户购物车为空",BusinessErrorCode.CART_NULL);
        }
        List<String> goodsStockIdStringList = new LinkedList<String>();
        for(CartInfoEntity ci : cartInfoList){
            goodsStockIdStringList.add(ci.getGoodsStockId().toString());
        }
        for(String goodsStockId : goodsStockIdArr){
            if(!goodsStockIdStringList.contains(goodsStockId)){
                LOG.info(requestId, "删除商品失败,购物车中无此商品", goodsStockId);
                throw new BusinessException("删除商品失败",BusinessErrorCode.GOODS_DELETE_ERROR);
            }
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userId", userId);
        paramMap.put("goodsStockIdArr", goodsStockIdArr);

        
        cartInfoRepository.deleteGoodsInCart(paramMap);

    }

	/**
	 * 查看购物车中商品信息
	 * 
	 * @param userId
	 * @throws BusinessException
	 */
    public List<ListCartDto> getGoodsInfoInCart(String requestId, String userId) throws BusinessException {
    	Long userIdVal = Long.valueOf(userId);

		List<GoodsInfoInCartEntity> goodsInfoInCartList = cartInfoRepository.getGoodsInfoInCart(userIdVal);

		if (null == goodsInfoInCartList || goodsInfoInCartList.isEmpty()) {
			LOG.info(requestId, "查询数据库购物车表数据", "数据为空");
			List<ListCartDto> list = new ArrayList<>();
			return list;
		} else {
			Date date = new Date();
			for (GoodsInfoInCartEntity goodsInfoInCart : goodsInfoInCartList) {
				String skuId;
				if (StringUtils.equals(goodsInfoInCart.getGoodsSource(),SourceType.WZ.getCode())) {
					String goodsLogoUrlNew = goodsInfoInCart.getGoodsBaseLogoUrl();
					goodsInfoInCart.setGoodsLogoUrlNew(
							imageService.getJDImageUrl(goodsLogoUrlNew, JdGoodsImageType.TYPEN3.getCode()));
					// 购物车中数量 为 0 的商品也标记已下架，让客户删除 (同步库存为0时导致的)
					if ((null != goodsInfoInCart.getDelistTime() && goodsInfoInCart.getDelistTime().before(date))
							|| goodsInfoInCart.getGoodsNum() == 0
							|| !GoodStatus.GOOD_UP.getCode().equals(goodsInfoInCart.getGoodsStatus())) {
						goodsInfoInCart.setIsDelete("00");// 失效
						goodsInfoInCart.setIsSelect("0");// 不选中
					}
					goodsInfoInCart.setStockCurrAmt(Long.parseLong("200"));// 设置商品的当前库存为200
					GoodsInfoEntity goodsInfoEntity = goodsService.selectByGoodsId(goodsInfoInCart.getGoodsId());
					goodsInfoInCart.setGoodsSkuAttr(goodsInfoEntity.getAttrDesc());
					skuId=goodsInfoEntity.getExternalId();
				} else {
					// 添加新的图片地址
					String goodsLogoUrlNew = EncodeUtils.base64Decode(goodsInfoInCart.getGoodsLogoUrl());
					goodsInfoInCart.setGoodsLogoUrlNew(imageService.getImageUrl(goodsLogoUrlNew));

					// 已过下架时间 或 库存为0， 标记该商品已下架 购物车中数量 为 0 的商品也标记已下架，让客户删除
					// (同步库存为0时导致的)
					if ((null != goodsInfoInCart.getDelistTime() && goodsInfoInCart.getDelistTime().before(date))
							|| null == goodsInfoInCart.getStockCurrAmt()
							|| goodsInfoInCart.getStockCurrAmt().intValue() == 0 || goodsInfoInCart.getGoodsNum() == 0
							|| !GoodStatus.GOOD_UP.getCode().equals(goodsInfoInCart.getGoodsStatus())) {
						goodsInfoInCart.setIsDelete("00");// 失效
						goodsInfoInCart.setIsSelect("0");// 不选中
					}
					// 如果非京东商品是多规格商品，则调用方法去取商品的规格描述
					List<GoodsAttrVal> goodsAttrValList = goodsAttrValService
							.queryGoodsAttrValsByGoodsId(goodsInfoInCart.getGoodsId());
					if (null != goodsAttrValList && goodsAttrValList.size() > 1) {
						// 获取非京东商品的多规格描述
						String goodsDesc = goodsService.getGoodsStockDesc(goodsInfoInCart.getGoodsStockId());
						goodsInfoInCart.setGoodsSkuAttr(goodsDesc);
					}
					Long goodsStockId=goodsInfoInCart.getGoodsStockId();
					GoodsStockInfoEntity goodsStockInfoEntity=goodsStockDao.getGoodsStockInfoEntityByStockId(goodsStockId);
					skuId=goodsStockInfoEntity.getSkuId();
				}

				// 计算商品折扣后价格
				BigDecimal goodsPrice = commonService.calculateGoodsPrice(goodsInfoInCart.getGoodsId(),
						goodsInfoInCart.getGoodsStockId());
				// 商品价格实时获取，不从购物车中取
				goodsInfoInCart.setGoodsSelectedPrice(goodsPrice);
				
				//根据skuId查询该规格是否参加了限时购活动
				LimitGoodsSkuVo limitGS=limitCommonService.selectLimitByGoodsId(userId,skuId);
				if(null !=limitGS  && StringUtils.equals("InProgress", limitGS.getLimitFalg())){
					goodsInfoInCart.setLimitFalg(true);
					goodsInfoInCart.setGoodsLimitPrice(limitGS.getActivityPrice());
					goodsInfoInCart.setLimitNum(limitGS.getLimitNum());
					goodsInfoInCart.setLimitPersonNum(limitGS.getLimitPersonNum());
					goodsInfoInCart.setLimitBuyActId(limitGS.getLimitBuyActId());
				}
			}

			// 取数据时已安装create_date desc 排序，把已下线商品放到最后
			List<GoodsInfoInCartEntity> list1 = new ArrayList<GoodsInfoInCartEntity>();
			List<GoodsInfoInCartEntity> list2 = new ArrayList<GoodsInfoInCartEntity>();
			for (GoodsInfoInCartEntity goodsInfo : goodsInfoInCartList) {
				if (goodsInfo.getIsDelete().equals("01")) {
					list1.add(goodsInfo);
				} else {
					list2.add(goodsInfo);
				}
			}

			// 按 商户编码(merchantCode) 分组
			Map<String, List<GoodsInfoInCartEntity>> resultMap = new LinkedHashMap<>();
			GoodsInfoInCartEntity goodsInfoInCart = new GoodsInfoInCartEntity();
			if (list1 != null && list1.size() > 0) {
				for (int i = 0; i < list1.size(); i++) {
					goodsInfoInCart = list1.get(i);
					// 根据goodsId查询该商品是否在有效活动中
					Long goodsId = goodsInfoInCart.getGoodsId();
					ProGroupGoodsBo proGroupGoodsBo = proGroupGoodsService.getByGoodsId(goodsId);
					if (null != proGroupGoodsBo && proGroupGoodsBo.isValidActivity()) {// 在活动中
						ProGroupGoods groupGoods = groupGoodsMapper.selectLatestByGoodsId(goodsId);
						Long activityId = groupGoods.getActivityId();
						ProActivityCfg activityCfg = activityCfgService.getById(activityId);
						if (null != activityCfg) {
							String act = "activity_" + activityId.toString();// 防止商户编码与活动id重复
							goodsInfoInCart.setProActivityId(activityId);// 活动id
							if (resultMap.containsKey(act)) {
								resultMap.get(act).add(goodsInfoInCart);
							} else {
								List<GoodsInfoInCartEntity> list = new ArrayList<GoodsInfoInCartEntity>();
								list.add(goodsInfoInCart);
								resultMap.put(act, list);
							}
						}
					} else if (resultMap.containsKey(goodsInfoInCart.getMerchantCode())) {
						resultMap.get(goodsInfoInCart.getMerchantCode()).add(goodsInfoInCart);
					} else {
						List<GoodsInfoInCartEntity> list = new ArrayList<GoodsInfoInCartEntity>();
						list.add(goodsInfoInCart);
						resultMap.put(goodsInfoInCart.getMerchantCode(), list);
					}
				}
			}

			List<ListCartDto> cartDtoList = new ArrayList<ListCartDto>();

			if (resultMap != null) {
				for (String key : resultMap.keySet()) {
					ListCartDto listCart = new ListCartDto();
					String[] keys = key.split("_");
					if (keys.length > 1) {
						String activityId = keys[1];
						Map<String, Object> activityCfgMap = goodsService
								.getCarActivityInfoByActivityId(Long.parseLong(activityId));
						String activityCfgDesc = (String) activityCfgMap.get("activityCfgDesc");
						String offerSill1 = (String) activityCfgMap.get("offerSill1");
						String discountAmonut1 = (String) activityCfgMap.get("discountAmonut1");
						String offerSill2 = (String) activityCfgMap.get("offerSill2");
						String discountAmonut2 = (String) activityCfgMap.get("discountAmonut2");
						ProActivityCfg activityCfg = activityCfgService.getById(Long.parseLong(activityId));
						if (activityCfg.getActivityType().equals("Y")) {
							listCart.setActivityCfg(activityCfgDesc);
						} else {
							listCart.setActivityCfg(null);// 无优惠不显示满减描述
						}
						listCart.setOfferSill1(offerSill1);
						listCart.setDiscountAmonut1(discountAmonut1);
						listCart.setOfferSill2(offerSill2);
						listCart.setDiscountAmount2(discountAmonut2);
					}
					listCart.setMerchantCode(key);
					listCart.setGoodsInfoInCartList(resultMap.get(key));
					cartDtoList.add(listCart);
				}
			}

			if (list2 != null && list2.size() > 0) {
				ListCartDto listCart = new ListCartDto();
				listCart.setMerchantCode("unavailability");
				listCart.setGoodsInfoInCartList(list2);
				cartDtoList.add(listCart);
			}

			return cartDtoList;
		}
    }

    /**
     * 获取购物车中商品总数量
     * 
     * @param userId
     * @return
     */
    public int getGoodsAmountInCart(String userId){
        Long userIdVal = Long.valueOf(userId);
        Integer goodsAmountInCart = cartInfoRepository.getGoodsAmountInCart(userIdVal);
        return null==goodsAmountInCart ? 0 : goodsAmountInCart.intValue();
    }

    /**
     * 同步客户端与数据库购物车信息 (此方法暂时废弃不用)
     * 
     * @param userId
     * @param goodsInfoList
     * @throws BusinessException 
     */
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> synchronizeCartInfo(String userId, List<GoodsStockIdNumDto> goodsInfoList) throws BusinessException {
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        
        Long userIdVal = Long.valueOf(userId);
        
        // 若未修改购物车中商品数量,则无需同步
        if(null != goodsInfoList && !goodsInfoList.isEmpty()){
            
            for(GoodsStockIdNumDto goodsInfoInCartDto : goodsInfoList){
                
                if(goodsInfoInCartDto.getGoodsNum() < 1){
                    LOGGER.error("同步购物车数据时,所选商品[{}]数量错误", goodsInfoInCartDto.getGoodsStockId());
                    throw new BusinessException("同步购物车数据时,所选商品数量错误");
                }
                
                CartInfoEntity cartDto = new CartInfoEntity();
                cartDto.setUserId(userIdVal);
                cartDto.setGoodsStockId(goodsInfoInCartDto.getGoodsStockId());
                cartDto.setGoodsNum(goodsInfoInCartDto.getGoodsNum());
                
                int updateGoodsNumFlag = cartInfoRepository.updateGoodsNum(cartDto);
                if(updateGoodsNumFlag != 1){
                    LOGGER.error("更新购物车中商品数量1失败[{}]", updateGoodsNumFlag);
                    throw new BusinessException("更新购物车中商品数量失败");
                }
                
            }
        }
        
        List<GoodsInfoInCartEntity> goodsInfoInCartList = cartInfoRepository.getGoodsInfoInCart(userIdVal);
        
        // 库存判断标记, 0 表示 购物车中有商品库存不足
        String synFlag = "1";
        String synMessage = "";
        if (null == goodsInfoInCartList || goodsInfoInCartList.isEmpty()) {
            LOGGER.info("synchronizeCartInfo->用户ID[{}],当前购物车为空", userId);
            throw new BusinessException("购物车为空");
        } else {
            for (GoodsInfoInCartEntity goodsInfoInCart : goodsInfoInCartList) {
               
                // 库存不足时，设置可购买数量为剩余库存量      库存为0时，标记商品已下架
                if(null == goodsInfoInCart.getStockCurrAmt() 
                        || goodsInfoInCart.getGoodsNum() > goodsInfoInCart.getStockCurrAmt().intValue()){
                    if(null == goodsInfoInCart.getStockCurrAmt() || goodsInfoInCart.getStockCurrAmt().intValue() == 0){
                        goodsInfoInCart.setGoodsNum(0);
                        goodsInfoInCart.setIsDelete("1");
                    } else {
                        synFlag = "0";
                        goodsInfoInCart.setGoodsNum(goodsInfoInCart.getStockCurrAmt().intValue());
                    }
                    synMessage += goodsInfoInCart.getGoodsName() + " ";
                    
                    // 更新购物车中该 商品购买数量为 当前库存量
                    CartInfoEntity cartDto = new CartInfoEntity();
                    cartDto.setUserId(userIdVal);
                    cartDto.setGoodsStockId(goodsInfoInCart.getGoodsStockId());
                    cartDto.setGoodsNum(goodsInfoInCart.getGoodsNum());
                    
                    int updateGoodsNumFlag =cartInfoRepository.updateGoodsNum(cartDto);
                    if(updateGoodsNumFlag != 1){
                        LOGGER.info("更新购物车中商品数量2失败[{}]", updateGoodsNumFlag);
                        throw new BusinessException("更新购物车中商品数量失败");
                    }
                }
                
            }
        }
        
        synMessage += synFlag.equals("0") ? "商品数量超出可购买范围!" : "";
        
        resultMap.put("synFlag", synFlag);
        resultMap.put("synMessage", synMessage);
        resultMap.put("goodsInfoInCartList", goodsInfoInCartList);
        
        int goodsAmountInCart = this.getNumOfTypeInCart(userId);
        resultMap.put("goodsAmountInCart", goodsAmountInCart);
        
        return resultMap;
    }
    
    /**
     * 修改购物车商品数量
     * 
     * @param userId
     * @param goodsInfoList
     * @throws BusinessException 
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateGoodsNumInCart(String requestId, String userId, List<GoodsStockIdNumDto> goodsInfoList) throws BusinessException {
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Long userIdVal = Long.valueOf(userId);
        
        // 1. 查询该用户购物车商品数据
        List<GoodsInfoInCartEntity> goodsInfoInCartList = cartInfoRepository.getGoodsInfoInCart(userIdVal);
        if(null == goodsInfoInCartList || goodsInfoInCartList.isEmpty()){
            LOG.info(requestId, "修改商品数量", "该用户购物车为空");
            throw new BusinessException("该用户购物车为空",BusinessErrorCode.CART_NULL);
        }
        
        Map<Long, GoodsInfoInCartEntity> cartInfoMap= new HashMap<Long, GoodsInfoInCartEntity>();
        List<Long> goodsStockIdList = new LinkedList<Long>();
        for(GoodsInfoInCartEntity cartInfo : goodsInfoInCartList){
        	if(SourceType.WZ.getCode().equals(cartInfo.getGoodsSource())){
        		cartInfo.setGoodsLogoUrlNew(imageService.getJDImageUrl(cartInfo.getGoodsBaseLogoUrl(),JdGoodsImageType.TYPEN3.getCode()));
        	}else{
                cartInfo.setGoodsLogoUrlNew(imageService.getImageUrl(cartInfo.getGoodsLogoUrl()));
        	}
            cartInfoMap.put(cartInfo.getGoodsStockId(), cartInfo);
            goodsStockIdList.add(cartInfo.getGoodsStockId());
        }
        
        // 库存判断标记, 0 表示 购物车中有商品库存不足
        String synFlag = "1";
        String synMessage = "";
        for(GoodsStockIdNumDto idNum : goodsInfoList){
            if(goodsStockIdList.contains(idNum.getGoodsStockId())){
                goodsStockIdList.remove(idNum.getGoodsStockId());
            } else {
                LOG.info(requestId, "购物车无此商品或商品库存id重复", String.valueOf(idNum.getGoodsStockId()));
                throw new BusinessException("无效的商品库存id",BusinessErrorCode.GOODS_STOCKID_ERROR);
            }
            
            if(idNum.getGoodsNum() < 1){
                LOG.info(requestId, "修改购物车商品数量时,提交的商品数量有误", String.valueOf(idNum.getGoodsNum()));
                throw new BusinessException("修改购物车商品数量时,所选商品数量错误",BusinessErrorCode.GOODS_AMOUNT_ERROR);
            }
            
            CartInfoEntity cartDto = new CartInfoEntity();
            cartDto.setUserId(userIdVal);
            cartDto.setGoodsStockId(idNum.getGoodsStockId());
            
        	// 查询商品库存信息
    		GoodsStockInfoEntity goodsStockInfo = goodsStockDao.select(idNum.getGoodsStockId());
    		if (null == goodsStockInfo) {
    			throw new BusinessException("无效的商品库存id", BusinessErrorCode.GOODS_NOT_EXIST);
    		}
    		GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsStockInfo.getGoodsId());
    		
			if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {
				cartDto.setGoodsNum(idNum.getGoodsNum());
			} else {
				int stockCurrAmt = cartInfoMap.get(idNum.getGoodsStockId()).getStockCurrAmt().intValue();
				if (stockCurrAmt < idNum.getGoodsNum()) {
					cartDto.setGoodsNum(stockCurrAmt);
					synFlag = "0";
					synMessage += cartInfoMap.get(idNum.getGoodsStockId()).getGoodsName() + " ";
				} else {
					cartDto.setGoodsNum(idNum.getGoodsNum());
				}
			}
            int updateGoodsNumFlag =cartInfoRepository.updateGoodsNum(cartDto);
            if(updateGoodsNumFlag != 1){
                LOG.info(requestId, "更新购物车中该商品数量失败", String.valueOf(idNum.getGoodsStockId()));
                throw new BusinessException("更新商品数量失败",BusinessErrorCode.GOODS_UPDATEINFO_ERROR);
            }
        }
        synMessage += synFlag.equals("0") ? "数量超出可购买范围!" : "";
        
        int goodsAmountInCart = this.getNumOfTypeInCart(userId);
        
        resultMap.put("synFlag", synFlag);
        resultMap.put("synMessage", synMessage);
        resultMap.put("goodsInfoInCartList", getGoodsInfoInCart(requestId, userId));
        resultMap.put("goodsAmountInCart", goodsAmountInCart);
        
        return resultMap;
    }

    /**
     * 获取购物车中商品种类数
     * 
     * @param userId
     * @return
     */
    public int getNumOfTypeInCart(String userId) {
        Long userIdVal = Long.valueOf(userId);
        CartInfoEntity cartDto = new CartInfoEntity();
        cartDto.setUserId(userIdVal);
        List<CartInfoEntity> cartInfoList = cartInfoRepository.filter(cartDto);
        return null == cartInfoList ? 0 : cartInfoList.size();
    }

    /**
     * 查看商品规格
     * 
     * @param goodsId
     * @return
     * @throws BusinessException 
     */
    public Map<String, Object> getGoodsStockSkuInfo(String requestId, String goodsId) throws BusinessException {
        
        Long goodsIdVal = Long.valueOf(goodsId);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        
        GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsIdVal);
        if(null == goodsInfo){
            LOG.info(requestId, "根据商品id查询商品信息", "数据为空");
            throw new BusinessException("无效的商品id",BusinessErrorCode.GOODS_ID_ERROR);
        }
		if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {
			Map<String, Object> jdSimilarSkuInfoMap = jdGoodsInfoService.jdSimilarSkuInfo(Long.parseLong(goodsInfo.getExternalId()));
			List<JdSimilarSkuTo> jdSimilarSkuToList=new ArrayList<>();
			//京东商品没有规格情况拼凑数据格式
            int jdSimilarSkuListSize= (int) jdSimilarSkuInfoMap.get("jdSimilarSkuListSize");
			if(jdSimilarSkuListSize==0){
			   jdSimilarSkuToList= getJdSimilarSkuToList(goodsIdVal);
			}else{
				jdSimilarSkuToList=(List<JdSimilarSkuTo>) jdSimilarSkuInfoMap.get("JdSimilarSkuToList");
			}
			resultMap.put("goodsSource", "jd");
			resultMap.put("JdSimilarSkuToList", jdSimilarSkuToList);
			resultMap.put("jdSimilarSkuList", jdSimilarSkuInfoMap.get("jdSimilarSkuList"));
			resultMap.put("jdSimilarSkuListSize", jdSimilarSkuInfoMap.get("jdSimilarSkuListSize"));
		} else {
			List<GoodsStockSkuDto> goodsStockSkuList = goodsStockDao.getGoodsStockSkuInfo(goodsIdVal);
			if (null == goodsInfo || goodsStockSkuList.isEmpty()) {
				LOG.info(requestId, "根据商品id查询商品库存信息", "数据为空");
				throw new BusinessException("无效的商品id", BusinessErrorCode.GOODS_ID_ERROR);
			}
			// 根据市场价和折扣率 计算商品价格
			for (GoodsStockSkuDto dto : goodsStockSkuList) {
				// 添加新的图片地址
				dto.setStockLogoNew(imageService.getImageUrl(EncodeUtils.base64Decode(dto.getStockLogo())));

                BigDecimal price = commonService.calculateGoodsPrice(goodsIdVal, dto.getGoodsStockId());
                dto.setGoodsPrice(price);// 商品价格
                dto.setGoodsPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));//商品首付价
             
			}
			resultMap.put("goodsSource", "");
			resultMap.put("goodsId", goodsInfo.getId());
			resultMap.put("goodsSkuType", goodsInfo.getGoodsSkuType());
			resultMap.put("goodsStockSkuList", goodsStockSkuList);
		}
        return resultMap;
    }
    /**
     * 查看商品规格(非京东商品多规格)
     * 
     * @param goodsId
     * @return
     * @throws BusinessException 
     */
    public Map<String, Object> getGoodsStockSkuInfoV3(String requestId, String goodsId,String goodsStockId) throws BusinessException {
        
        Long goodsIdVal = Long.valueOf(goodsId);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        
        GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsIdVal);
        if(null == goodsInfo){
            LOG.info(requestId, "根据商品id查询商品信息", "数据为空");
            throw new BusinessException("无效的商品id",BusinessErrorCode.GOODS_ID_ERROR);
        }
		if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {
			Map<String, Object> jdSimilarSkuInfoMap = jdGoodsInfoService.jdSimilarSkuInfo(Long.parseLong(goodsInfo.getExternalId()));
			List<JdSimilarSkuTo> jdSimilarSkuToList=new ArrayList<>();
			//京东商品没有规格情况拼凑数据格式
            int jdSimilarSkuListSize= (int) jdSimilarSkuInfoMap.get("jdSimilarSkuListSize");
			if(jdSimilarSkuListSize==0){
			   jdSimilarSkuToList= getJdSimilarSkuToList(goodsIdVal);
			}else{
				jdSimilarSkuToList=(List<JdSimilarSkuTo>) jdSimilarSkuInfoMap.get("JdSimilarSkuToList");
			}
			resultMap.put("source", "jd");
			resultMap.put("JdSimilarSkuToList", jdSimilarSkuToList);
			resultMap.put("jdSimilarSkuList", jdSimilarSkuInfoMap.get("jdSimilarSkuList"));
			resultMap.put("jdSimilarSkuListSize", jdSimilarSkuInfoMap.get("jdSimilarSkuListSize"));
		} else {
			//非京东商品多规格
			List<GoodsStockSkuDto> goodsStockSkuList = goodsStockDao.getGoodsStockSkuInfo(goodsIdVal);
			if (null == goodsInfo || goodsStockSkuList.isEmpty()) {
				LOG.info(requestId, "根据商品id查询商品库存信息", "数据为空");
				throw new BusinessException("无效的商品id", BusinessErrorCode.GOODS_ID_ERROR);
			}
			
			Map<String,Object> returnMap2=new HashMap<>();
			Boolean isUnSupport=false;
        	returnMap2.put("isUnSupport", isUnSupport);
        	returnMap2.put("goodsStockId", goodsStockId);
			goodsService.loadGoodsBasicInfoById2(goodsIdVal, returnMap2);
			
			resultMap.put("source", "notJd");
			resultMap.put("JdSimilarSkuToList", returnMap2.get("JdSimilarSkuToList"));
			resultMap.put("jdSimilarSkuList", returnMap2.get("jdSimilarSkuList"));
			resultMap.put("jdSimilarSkuListSize", returnMap2.get("jdSimilarSkuListSize"));
		}
		  // 京东商品没有规格情况拼凑数据格式
          int jdSimilarSkuListSize = (int) resultMap.get("jdSimilarSkuListSize");
	      List<JdSimilarSku> jdSimilarSkuList = (List<JdSimilarSku>) resultMap.get("jdSimilarSkuList");
          if (jdSimilarSkuListSize == 0 || null ==jdSimilarSkuList || jdSimilarSkuList.isEmpty()) {
              List<JdSimilarSkuTo> JdSimilarSkuToList = (List<JdSimilarSkuTo>) resultMap.get("JdSimilarSkuToList");
              JdSimilarSkuTo jdSimilarSkuTo = new JdSimilarSkuTo();
              JdSimilarSkuVo jdSimilarSkuVo = new JdSimilarSkuVo();
              jdSimilarSkuVo.setGoodsId(goodsId.toString());
              String source =(String) resultMap.get("source");
  			  List<GoodsStockSkuDto> goodsStockSkuList = goodsStockDao.getGoodsStockSkuInfo(goodsIdVal);
  			 //价格计算
              if (goodsStockSkuList.size() == 1) {
                  BigDecimal price = commonService.calculateGoodsPrice(goodsIdVal, Long.parseLong(goodsStockId));
                  goodsInfo.setGoodsPrice(price);
                  goodsInfo.setFirstPrice((new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));
              }
              if(StringUtils.equals(SourceType.WZ.getCode(), source)){
              	 jdSimilarSkuVo.setSkuId(goodsInfo.getExternalId());
              }else{
              	jdSimilarSkuVo.setSkuId(goodsStockId);
              	jdSimilarSkuVo.setStockCurrAmt(goodsStockSkuList.get(0).getStockCurrAmt());
              }
              jdSimilarSkuVo.setGoodsStockId(goodsStockId);
              jdSimilarSkuVo.setPrice(goodsInfo.getGoodsPrice());
              jdSimilarSkuVo.setPriceFirst(goodsInfo.getFirstPrice());
              jdSimilarSkuVo.setStockDesc("");
              jdSimilarSkuTo.setSkuIdOrder("");
              jdSimilarSkuTo.setJdSimilarSkuVo(jdSimilarSkuVo);
              JdSimilarSkuToList.add(jdSimilarSkuTo);
              resultMap.put("JdSimilarSkuToList", JdSimilarSkuToList);
          }
        return resultMap;
    }
    /**
     *  京东商品没有规格情况拼凑数据格式
     * @throws BusinessException 
     */
	public List<JdSimilarSkuTo> getJdSimilarSkuToList(Long goodsId) throws BusinessException {
		GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsId);
		List<GoodsStockInfoEntity> jdGoodsStockInfoList = goodsStockDao.loadByGoodsId(goodsId);
		List<JdSimilarSkuTo> JdSimilarSkuToList = new ArrayList<>();
		JdSimilarSkuTo jdSimilarSkuTo = new JdSimilarSkuTo();
		JdSimilarSkuVo jdSimilarSkuVo = new JdSimilarSkuVo();
		if (jdGoodsStockInfoList.size() == 1) {
			BigDecimal price = commonService.calculateGoodsPrice(goodsId, jdGoodsStockInfoList.get(0).getId());

			jdSimilarSkuVo.setGoodsId(goodsId.toString());
			jdSimilarSkuVo.setSkuId(goodsInfo.getExternalId());
			jdSimilarSkuVo.setGoodsStockId(jdGoodsStockInfoList.get(0).getId().toString());
			jdSimilarSkuVo.setPrice(price);
			jdSimilarSkuVo.setPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));
			jdSimilarSkuVo.setStockDesc("");
			jdSimilarSkuTo.setSkuIdOrder("");
			jdSimilarSkuTo.setJdSimilarSkuVo(jdSimilarSkuVo);
			JdSimilarSkuToList.add(jdSimilarSkuTo);
		}
		return JdSimilarSkuToList;
	}
    /**
     * 修改商品规格
     * 
     * @param preGoodsStockId   修改前商品库存id
     * @param secGoodsStockId   修改后商品库存id
     * @param num2 
     * @throws BusinessException 
     */
    @Transactional(rollbackFor = Exception.class)
    public GoodsInfoInCartEntity reelectSku(String requestId, String userId, String goodsId, String preGoodsStockId, String secGoodsStockId, String num) throws BusinessException {
        
        Long userIdVal = Long.valueOf(userId);
        Long goodsIdVal = Long.valueOf(goodsId);
        Long preGoodsStockIdVal = Long.valueOf(preGoodsStockId);
        Long secGoodsStockIdVal = Long.valueOf(secGoodsStockId);
        int numVal = Integer.parseInt(num);
        
		List<GoodsStockInfoEntity> goodsStockInfoList = new ArrayList<>();
        Map<Long, GoodsStockInfoEntity> resultMap= new HashMap<Long, GoodsStockInfoEntity>();
        GoodsInfoInCartEntity goodsInfoInCart = new GoodsInfoInCartEntity();
        GoodsInfoEntity goodsInfo=new GoodsInfoEntity();
        
        GoodsStockInfoEntity preGoodsStockEntity=goodsStockDao.select(preGoodsStockIdVal);
        GoodsStockInfoEntity secGoodsStockEntity=goodsStockDao.select(secGoodsStockIdVal);

        String externalId=null;
		if (SourceType.WZ.getCode().equals(preGoodsStockEntity.getGoodsSource()) && SourceType.WZ.getCode().equals(secGoodsStockEntity.getGoodsSource())) {
			// 查询商品基本信息，返回客户端该商品单条信息
			goodsInfo =goodsInfoDao.select(secGoodsStockEntity.getGoodsId());
			goodsInfoInCart.setGoodsLogoUrl(imageService.getJDImageUrl(secGoodsStockEntity.getGoodsLogoUrl(),JdGoodsImageType.TYPEN3.getCode()));
			goodsInfoInCart.setGoodsLogoUrlNew(imageService.getJDImageUrl(secGoodsStockEntity.getGoodsLogoUrl(),JdGoodsImageType.TYPEN3.getCode()));
			goodsInfoInCart.setStockCurrAmt(Long.parseLong("200"));
			goodsInfoInCart.setGoodsSkuAttr(goodsInfo.getAttrDesc());//商品描述
			externalId=goodsInfo.getExternalId();
		} else {
	        // 1.校验 preGoodsStockId、secGoodsStockId 是否属于 goodsId
			goodsStockInfoList = goodsStockDao.loadByGoodsId(goodsIdVal);
			if (null == goodsStockInfoList || goodsStockInfoList.isEmpty()) {
				LOG.info(requestId, "根据商品id查询商品信息", "数据为空");
				throw new BusinessException("无效的商品id", BusinessErrorCode.GOODS_ID_ERROR);
			}
			for (GoodsStockInfoEntity goodsStockInfo : goodsStockInfoList) {
				goodsStockInfo.setStockLogoNew(imageService.getImageUrl(goodsStockInfo.getStockLogo()));
				resultMap.put(goodsStockInfo.getGoodsStockId(), goodsStockInfo);
			}
			if (!resultMap.containsKey(preGoodsStockIdVal) || !resultMap.containsKey(secGoodsStockIdVal)) {
				LOG.info(requestId, "商品库存id与商品id不匹配", "");
				throw new BusinessException("无效的商品id或库存id", BusinessErrorCode.GOODS_ID_ERROR);
			}
	        goodsInfoInCart.setGoodsLogoUrl(resultMap.get(secGoodsStockIdVal).getStockLogo());
	        goodsInfoInCart.setGoodsLogoUrlNew(imageService.getImageUrl(resultMap.get(secGoodsStockIdVal).getStockLogo()));
	        goodsInfoInCart.setGoodsSkuAttr(resultMap.get(secGoodsStockIdVal).getGoodsSkuAttr());
	        goodsInfoInCart.setStockCurrAmt(resultMap.get(secGoodsStockIdVal).getStockCurrAmt());
	        goodsInfoInCart.setDelistTime(goodsInfo.getDelistTime());
	        externalId=secGoodsStockEntity.getSkuId();
		}
		//根据skuId查询该规格是否参加了限时购活动
		LimitGoodsSkuVo limitGS=limitCommonService.selectLimitByGoodsId(userId,externalId);
		if(null !=limitGS  && StringUtils.equals("InProgress", limitGS.getLimitFalg())){
			goodsInfoInCart.setLimitFalg(true);
			goodsInfoInCart.setGoodsLimitPrice(limitGS.getActivityPrice());
			goodsInfoInCart.setLimitNum(limitGS.getLimitNum());
			goodsInfoInCart.setLimitPersonNum(limitGS.getLimitPersonNum());
			goodsInfoInCart.setLimitBuyActId(limitGS.getId());
		}
        // 2.删除购物车中原商品
        this.deleteGoodsInCart(requestId, userIdVal, new String[]{preGoodsStockId});
        
        // 3.添加新规格商品到购物车
        this.addGoodsToCart(requestId, userId, secGoodsStockId, num);
        
        // 4.查询商品基本信息，返回客户端该商品单条信息
//        GoodsInfoEntity goodsInfo = goodsInfoDao.select(goodsIdVal);
        
        // 5.计算商品折扣后价格
        BigDecimal goodsPrice = commonService.calculateGoodsPrice(goodsIdVal, secGoodsStockIdVal);
        
//        GoodsInfoInCartEntity goodsInfoInCart = new GoodsInfoInCartEntity();
        goodsInfoInCart.setUserId(userIdVal);
        goodsInfoInCart.setGoodsId(goodsIdVal);
        goodsInfoInCart.setGoodsStockId(secGoodsStockIdVal);
        goodsInfoInCart.setGoodsName(goodsInfo.getGoodsName());
        goodsInfoInCart.setGoodsSelectedPrice(goodsPrice);
        goodsInfoInCart.setGoodsNum(numVal);
        goodsInfoInCart.setIsDelete(goodsInfo.getIsDelete());
        goodsInfoInCart.setIsSelect("1");
        goodsInfoInCart.setMerchantCode(goodsInfo.getMerchantCode());
        
        return goodsInfoInCart;
    }

    /**
     * 购物车商品展示
     * 
     * @param userId
     * @return
     * @throws BusinessException 
     */
    public List<ListCartDto> getCartDtoList(String requestId, String userId) throws BusinessException {
        
        // 获取购物车中商品基本信息
        List<GoodsInfoInCartEntity> dataList = null;//getGoodsInfoInCart(requestId, userId);
        
        // 按 商户编码(merchantCode) 分组
        Map<String, List<GoodsInfoInCartEntity>> resultMap= new HashMap<String, List<GoodsInfoInCartEntity>>();
        GoodsInfoInCartEntity goodsInfoInCart = new GoodsInfoInCartEntity();
        for(int i=0; i<dataList.size(); i++){
            goodsInfoInCart = dataList.get(i);
            if (resultMap.containsKey(goodsInfoInCart.getMerchantCode())) {
                resultMap.get(goodsInfoInCart.getMerchantCode()).add(goodsInfoInCart);
            } else {
                List<GoodsInfoInCartEntity> list= new ArrayList<GoodsInfoInCartEntity>();
                list.add(goodsInfoInCart);
                resultMap.put(goodsInfoInCart.getMerchantCode(), list);
            }
        }
        
        
        List<ListCartDto> cartDtoList = new ArrayList<ListCartDto>();
        
        for(String key : resultMap.keySet()){
            ListCartDto listCart = new ListCartDto();
            listCart.setMerchantCode(key);
            listCart.setGoodsInfoInCartList(resultMap.get(key));
            cartDtoList.add(listCart);
        }
        return cartDtoList;
    }

    /**
     * 同步 购物车商品 勾选标记
     * 
     * @param userId
     * @param goodsIsSelectList
     * @throws BusinessException 
     */
    public void synIsSelect(String requestId, String userId, List<GoodsIsSelectDto> goodsIsSelectList) throws BusinessException {
        
        Long userIdVal = Long.valueOf(userId);
        
        CartInfoEntity cto = new CartInfoEntity();
        cto.setUserId(userIdVal);
        List<CartInfoEntity> cartInfoList = cartInfoRepository.filter(cto);
        if(null == cartInfoList || cartInfoList.isEmpty()){
            LOG.info(requestId, "查询购物车商品信息", "数据为空");
            throw new BusinessException("无效的用户名!",BusinessErrorCode.GOODS_NOT_EXIST);
        }
        
        // 存放改该用户购物车中商品id的 list
        List<String> goodsStockIdList = new LinkedList<String>();
        for(CartInfoEntity info : cartInfoList){
            goodsStockIdList.add(info.getGoodsStockId().toString());
        }
        
        for(GoodsIsSelectDto gis : goodsIsSelectList){
            
            if(goodsStockIdList.contains(gis.getGoodsStockId())){
                goodsStockIdList.remove(gis.getGoodsStockId());
            } else {
                LOG.info(requestId, "购物车中无此商品或提交的商品库存id重复", "");
                throw new BusinessException("无效的商品库存id",BusinessErrorCode.GOODS_STOCKID_ERROR);
            }
            
            if(!YesNo.isLegal(gis.getIsSelect())){
                LOG.info(requestId, "提交的勾选标记字段不合法", "");
                throw new BusinessException("商品勾选标记字段不合法",BusinessErrorCode.GOODS_CHECKOPTION_ERROR);
            }
            
            CartInfoEntity cartDto = new CartInfoEntity();
            cartDto.setUserId(Long.valueOf(userId));
            cartDto.setGoodsStockId(Long.valueOf(gis.getGoodsStockId()));
            cartDto.setIsSelect(gis.getIsSelect());
            
            goodsInfoDao.synIsSelect(cartDto);
        }
        
    }

}
