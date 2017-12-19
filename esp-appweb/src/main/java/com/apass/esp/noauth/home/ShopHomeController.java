package com.apass.esp.noauth.home;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.apass.esp.common.code.BusinessErrorCode;
import com.apass.esp.domain.Response;
import com.apass.esp.domain.dto.ProGroupGoodsBo;
import com.apass.esp.domain.entity.Category;
import com.apass.esp.domain.entity.LimitBuyAct;
import com.apass.esp.domain.entity.WorkCityJd;
import com.apass.esp.domain.entity.activity.ActivityInfoEntity;
import com.apass.esp.domain.entity.activity.LimitGoodsSkuVo;
import com.apass.esp.domain.entity.address.AddressInfoEntity;
import com.apass.esp.domain.entity.banner.BannerInfoEntity;
import com.apass.esp.domain.entity.common.DictDTO;
import com.apass.esp.domain.entity.common.ResponseInit;
import com.apass.esp.domain.entity.goods.GoodsBasicInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsStockInfoEntity;
import com.apass.esp.domain.entity.jd.JdSimilarSku;
import com.apass.esp.domain.entity.jd.JdSimilarSkuTo;
import com.apass.esp.domain.entity.jd.JdSimilarSkuVo;
import com.apass.esp.domain.enums.ActivityInfoStatus;
import com.apass.esp.domain.enums.BannerType;
import com.apass.esp.domain.enums.CategorySort;
import com.apass.esp.domain.enums.CityJdEnums;
import com.apass.esp.domain.enums.GoodStatus;
import com.apass.esp.domain.enums.LimitBuyStatus;
import com.apass.esp.domain.enums.SourceType;
import com.apass.esp.domain.utils.ConstantsUtils;
import com.apass.esp.domain.vo.CategoryVo;
import com.apass.esp.domain.vo.LimitBuyActBannerVo;
import com.apass.esp.domain.vo.MyCouponVo;
import com.apass.esp.domain.vo.OtherCategoryGoodsVo;
import com.apass.esp.repository.activity.ActivityInfoRepository;
import com.apass.esp.repository.goods.GoodsStockInfoRepository;
import com.apass.esp.search.condition.GoodsSearchCondition;
import com.apass.esp.search.entity.Goods;
import com.apass.esp.search.entity.GoodsVo;
import com.apass.esp.search.enums.SortMode;
import com.apass.esp.search.manager.IndexManager;
import com.apass.esp.service.activity.LimitBuyActService;
import com.apass.esp.service.activity.LimitCommonService;
import com.apass.esp.service.address.AddressService;
import com.apass.esp.service.banner.BannerInfoService;
import com.apass.esp.service.cart.ShoppingCartService;
import com.apass.esp.service.category.CategoryInfoService;
import com.apass.esp.service.common.CommonService;
import com.apass.esp.service.common.ImageService;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.jd.JdGoodsInfoService;
import com.apass.esp.service.nation.NationService;
import com.apass.esp.service.offer.MyCouponManagerService;
import com.apass.esp.service.offer.ProGroupGoodsService;
import com.apass.esp.service.order.OrderService;
import com.apass.esp.service.wz.WeiZhiGoodsInfoService;
import com.apass.esp.service.wz.WeiZhiProductService;
import com.apass.esp.third.party.jd.entity.base.Region;
import com.apass.esp.utils.ValidateUtils;
import com.apass.gfb.framework.environment.SystemEnvConfig;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.mybatis.page.Pagination;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.gfb.framework.utils.EncodeUtils;
import com.apass.gfb.framework.utils.GsonUtils;
import com.google.common.collect.Lists;

/**
 * 首页
 *
 * @description
 *
 * @author liuming
 * @version $Id: ShopHomeController.java, v 0.1 2016年12月19日 下午3:33:14 liuming Exp $
 */
@Path("/shop/index")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShopHomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopHomeController.class);

    @Autowired
    private GoodsService goodService;

    @Autowired
    private BannerInfoService bannerService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CategoryInfoService categoryInfoService;

    @Autowired
    private GoodsStockInfoRepository goodsStockInfoRepository;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private JdGoodsInfoService jdGoodsInfoService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private NationService nationService;

    @Autowired
    private ActivityInfoRepository actityInfoDao;
    @Autowired
    private OrderService orderService;

    @Value("${esp.image.uri}")
    private String espImageUrl;

    @Autowired
    private SystemEnvConfig systemEnvConfig;

    @Autowired
    private ProGroupGoodsService proGroupGoodsService;
    @Autowired
    private MyCouponManagerService myCouponManagerService;
    @Autowired
    private WeiZhiProductService weiZhiProductService;
    @Autowired
    private WeiZhiGoodsInfoService weiZhiGoodsInfoService;
    @Autowired
    private LimitBuyActService limitBuyActService;
    @Autowired
    private LimitCommonService limitCommonService;
    /**
     * 首页初始化 加载banner和精品商品
     *
     * @return
     */
    @POST
    @Path("/init")
    public Response indexInit() {
        try {
            Map<String, Object> returnMap = new HashMap<String, Object>();
            List<BannerInfoEntity> banners = bannerService.loadIndexBanners(ConstantsUtils.BANNERTYPEINDEX);
            for (BannerInfoEntity banner : banners) {
                banner.setActivityUrl(banner.getActivityUrl());

                banner.setBannerImgUrlNew(imageService.getImageUrl(banner.getBannerImgUrl()));

                banner.setBannerImgUrl(EncodeUtils.base64Encode(banner.getBannerImgUrl()));
            }

            Pagination<GoodsBasicInfoEntity> recommendGoods = goodService.loadRecommendGoods(0, 10);
            returnMap.put("banners", banners);
            returnMap.put("recommendGoods", recommendGoods.getDataList());
            ArrayList<ResponseInit> mapArrayList = new ArrayList<>(2);
            ResponseInit responseInit = new ResponseInit();
            responseInit.setTitle("商城首付价购物指引");
            responseInit.setUrl("http://ajqh.download.apass.cn/activity/20170912/index.html");
            ResponseInit responseInit2 = new ResponseInit();
            responseInit2.setTitle("客户谨慎还款重要提示");
            if (systemEnvConfig.isPROD()) {
                responseInit2.setUrl("http://ajqh.app.apass.cn/#/Announcement");
            } else {
                responseInit2.setUrl("http://gfbapp.vcash.cn/#/Announcement");
            }

            mapArrayList.add(responseInit);
            mapArrayList.add(responseInit2);
            returnMap.put("guide", mapArrayList);
            for (GoodsBasicInfoEntity goods : recommendGoods.getDataList()) {
                ActivityInfoEntity param = new ActivityInfoEntity();
                param.setGoodsId(goods.getGoodId());
                param.setStatus(ActivityInfoStatus.EFFECTIVE.getCode());
                List<ActivityInfoEntity> activitys = actityInfoDao.filter(param);
                Map<String, Object> result = new HashMap<>();
                if (null != activitys && activitys.size() > 0) {
                    result = goodService.getMinPriceGoods(goods.getGoodId());
                    BigDecimal price = (BigDecimal) result.get("minPrice");
                    Long minPriceStockId = (Long) result.get("minPriceStockId");
                    goods.setGoodsPrice(price);
                    goods.setGoodsPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2,
                            BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
                    goods.setGoodsStockId(minPriceStockId);
                } else {
                    BigDecimal price = commonService.calculateGoodsPrice(goods.getGoodId(),
                            goods.getGoodsStockId());
                    goods.setGoodsPrice(price);
                    goods.setGoodsPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2,
                            BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
                }
                if (SourceType.JD.getCode().equals(goods.getSource())) {
                    goods.setGoodsLogoUrlNew("http://img13.360buyimg.com/n3/" + goods.getGoodsLogoUrl());
                    if (goods.getGoodsSiftUrl().contains("eshop")) {
                        goods.setGoodsSiftUrlNew(imageService.getImageUrl(goods.getGoodsSiftUrl()));
                    } else {
                        goods.setGoodsSiftUrlNew("http://img13.360buyimg.com/n3/" + goods.getGoodsLogoUrl());
                    }
                } else if (SourceType.WZ.getCode().equals(goods.getSource())) {
                    goods.setGoodsLogoUrlNew("http://img13.360buyimg.com/n3/" + goods.getGoodsLogoUrl());
                    if (goods.getGoodsSiftUrl().contains("eshop")) {
                        goods.setGoodsSiftUrlNew(imageService.getImageUrl(goods.getGoodsSiftUrl()));
                    } else {
                        goods.setGoodsSiftUrlNew("http://img13.360buyimg.com/n3/" + goods.getGoodsLogoUrl());
                    }
                } else {
                    // 电商3期511 20170517 根据商品Id查询所有商品库存中市场价格最高的商品的市场价
                    Long marketPrice = goodsStockInfoRepository.getMaxMarketPriceByGoodsId(goods.getGoodId());
                    goods.setMarketPrice(new BigDecimal(marketPrice));
                    goods.setGoodsLogoUrlNew(imageService.getImageUrl(goods.getGoodsLogoUrl()));
                    goods.setGoodsSiftUrlNew(imageService.getImageUrl(goods.getGoodsSiftUrl()));
                    goods.setGoodsLogoUrl(EncodeUtils.base64Encode(goods.getGoodsLogoUrl()));
                    goods.setGoodsSiftUrl(EncodeUtils.base64Encode(goods.getGoodsSiftUrl()));
                }
            }

            //限时购:开始时间和banner图
            //查询是当前是否有限时购活动：无-->不显示 有显示入口
            LimitBuyAct limitBuyAct = new LimitBuyAct();
            limitBuyAct.setStatus((byte)Integer.parseInt(LimitBuyStatus.PROCEED.getValue()));
            List<LimitBuyAct> limitBuyActs = limitBuyActService.readEntityList(limitBuyAct);
            if(!CollectionUtils.isEmpty(limitBuyActs)){
                LimitBuyActBannerVo limitBuyActBannerVo = new LimitBuyActBannerVo();//返回app值
                limitBuyAct = limitBuyActs.get(0);
                //TODO 发生产时要在对应目录上传图片 /data/nfs/gfb/eshop/banner/limitbuy
                limitBuyActBannerVo.setImgurl(espImageUrl + "/static"+ "/eshop/banner/limitbuy/20171218164912.png");
//                String startDate = DateFormatUtil.dateToString(limitBuyAct.getStartDate(),DateFormatUtil.YYYY_MM_DD_HH_MM_SS);
                limitBuyActBannerVo.setStartDate(limitBuyAct.getStartDate());
                limitBuyActBannerVo.setEndDate(limitBuyAct.getEndDate());
                returnMap.put("limitBuyActBannerVo",limitBuyActBannerVo);
            }


            return Response.successResponse(returnMap);
        } catch (Exception e) {
            LOGGER.error("indexInit fail", e);
            LOGGER.error("首页加载失败");
            return Response.fail(BusinessErrorCode.LOAD_INFO_FAILED);
        }
    }

    /**
     * 加载商品列表
     *
     * @return
     */
    @POST
    @Path("/loadGoodsList")
    public Response loadGoodsList(Map<String, Object> paramMap) {
        try {
            Map<String, Object> returnMap = new HashMap<String, Object>();
            String flage = CommonUtils.getValue(paramMap, "flage");// 标记是精选还是类目
            List<GoodsBasicInfoEntity> goodsList = null;
            if (null != flage && flage.equals("category")) {
                String categoryId = CommonUtils.getValue(paramMap, "categoryId");// 类目Id
                String page = CommonUtils.getValue(paramMap, "page");
                String rows = CommonUtils.getValue(paramMap, "rows");
                if (StringUtils.isEmpty(categoryId)) {
                    LOGGER.error("类目id不能空！");
                    return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
                }
                GoodsBasicInfoEntity goodsInfoEntity = new GoodsBasicInfoEntity();
                goodsInfoEntity.setCategoryId1(Long.parseLong(categoryId));// 设置1级类目Id
                Pagination<GoodsBasicInfoEntity> goodsPageList = goodsService.loadGoodsByCategoryId(
                        goodsInfoEntity, page, rows);
                goodsList = goodsPageList.getDataList();
                returnMap.put("totalCount", goodsPageList.getTotalCount());
                // 设置类目张的banner
                List<BannerInfoEntity> banners = new ArrayList<BannerInfoEntity>();
                BannerInfoEntity bity = new BannerInfoEntity();

                Category category = categoryInfoService.selectNameById(Long.parseLong(categoryId));
                if ("1".equals(String.valueOf(category.getSortOrder()))) {// 家用电器banner图
                    bity.setBannerImgUrlNew(espImageUrl + "/static/eshop/other/categoryElectricBanner.png");
                    bity.setBannerImgUrl(espImageUrl + "/static/eshop/other/categoryElectricBanner.png");
                } else if ("2".equals(String.valueOf(category.getSortOrder()))) {// 家居百货banner图
                    bity.setBannerImgUrlNew(espImageUrl + "/static/eshop/other/categoryDepotBanner.png");
                    bity.setBannerImgUrl(espImageUrl + "/static/eshop/other/categoryDepotBanner.png");

                } else if ("3".equals(String.valueOf(category.getSortOrder()))) {// 美妆生活banner图
                    bity.setBannerImgUrlNew(espImageUrl + "/static/eshop/other/categoryBeautyBanner.png");
                    bity.setBannerImgUrl(espImageUrl + "/static/eshop/other/categoryBeautyBanner.png");
                }
                banners.add(bity);
                returnMap.put("banners", banners);
            } else if (null != flage && flage.equals("recommend")) {
                String page = CommonUtils.getValue(paramMap, "page");
                String rows = CommonUtils.getValue(paramMap, "rows");
                int pageSize = Integer.MAX_VALUE;
                int pageIndex = 1;
                if (StringUtils.isNotBlank(page) && StringUtils.isNotBlank(rows)) {
                    try {
                        pageIndex = Integer.parseInt(page);
                        pageSize = Integer.parseInt(rows);
                    } catch (Exception e) {

                    }
                }
                int pageBegin = pageSize * (pageIndex - 1);
                Pagination<GoodsBasicInfoEntity> goodsPageList = goodService.loadRecommendGoods(pageBegin, pageSize);//加载精选商品
                //goodsList = goodService.loadRecommendGoodsList();// 加载精选商品列表
                goodsList = goodsPageList.getDataList();
                returnMap.put("totalCount", goodsPageList.getTotalCount());

                List<BannerInfoEntity> banners = bannerService.loadIndexBanners(BannerType.BANNER_SIFT
                        .getIdentify());
                for (BannerInfoEntity banner : banners) {
                    banner.setActivityUrl(banner.getActivityUrl());

                    banner.setBannerImgUrlNew(imageService.getImageUrl(banner.getBannerImgUrl()));

                    banner.setBannerImgUrl(EncodeUtils.base64Encode(banner.getBannerImgUrl()));
                }
                returnMap.put("banners", banners);
            } else {
                goodsList = goodService.loadGoodsList();// 加载所以商品

                List<BannerInfoEntity> banners = bannerService.loadIndexBanners(BannerType.BANNER_SIFT
                        .getIdentify());
                for (BannerInfoEntity banner : banners) {
                    // banner.setActivityUrl(EncodeUtils.base64Encode(banner.getActivityUrl()));
                    banner.setActivityUrl(banner.getActivityUrl());

                    banner.setBannerImgUrlNew(imageService.getImageUrl(banner.getBannerImgUrl()));

                    banner.setBannerImgUrl(EncodeUtils.base64Encode(banner.getBannerImgUrl()));
                }
                returnMap.put("banners", banners);
            }

            for (GoodsBasicInfoEntity goodsInfo : goodsList) {
                if (null != goodsInfo.getGoodId() && null != goodsInfo.getGoodsStockId()) {
                    ActivityInfoEntity param = new ActivityInfoEntity();
                    param.setGoodsId(goodsInfo.getGoodId());
                    param.setStatus(ActivityInfoStatus.EFFECTIVE.getCode());
                    List<ActivityInfoEntity> activitys = actityInfoDao.filter(param);
                    Map<String, Object> result = new HashMap<>();
                    if (null != activitys && activitys.size() > 0) {
                        result = goodService.getMinPriceGoods(goodsInfo.getGoodId());
                        BigDecimal price = (BigDecimal) result.get("minPrice");
                        Long minPriceStockId = (Long) result.get("minPriceStockId");
                        goodsInfo.setGoodsPrice(price);
                        goodsInfo.setGoodsPriceFirst(
                                (new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
                        goodsInfo.setGoodsStockId(minPriceStockId);
                    } else {
                        BigDecimal price = commonService.calculateGoodsPrice(goodsInfo.getGoodId(),
                                goodsInfo.getGoodsStockId());
                        goodsInfo.setGoodsPrice(price);
                        goodsInfo.setGoodsPriceFirst(
                                (new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
                    }
                    // 电商3期511 20170517 根据商品Id查询所有商品库存中市场价格最高的商品的市场价
                    // Long marketPrice =
                    // goodsStockInfoRepository.getMaxMarketPriceByGoodsId(goodsInfo
                    // .getGoodId());
                    // goodsInfo.setMarketPrice(new BigDecimal(marketPrice));
                    String logoUrl = goodsInfo.getGoodsLogoUrl();
                    String siftUrl = goodsInfo.getGoodsSiftUrl();
                    if (StringUtils.equals(goodsInfo.getSource(), SourceType.JD.getCode())) {
                        goodsInfo.setGoodsLogoUrlNew("http://img13.360buyimg.com/n1/" + goodsInfo.getGoodsLogoUrl());
                        goodsInfo.setGoodsSiftUrlNew("http://img13.360buyimg.com/n1/" + goodsInfo.getGoodsLogoUrl());
                    } else if (StringUtils.equals(goodsInfo.getSource(), SourceType.WZ.getCode())) {
                            goodsInfo.setGoodsLogoUrlNew("http://img13.360buyimg.com/n3/" + goodsInfo.getGoodsLogoUrl());
                            goodsInfo.setGoodsSiftUrlNew("http://img13.360buyimg.com/n3/" + goodsInfo.getGoodsLogoUrl());
                        } else {
                            goodsInfo.setGoodsLogoUrlNew(imageService.getImageUrl(logoUrl));
                            goodsInfo.setGoodsSiftUrlNew(imageService.getImageUrl(siftUrl));
                        }
                        goodsInfo.setGoodsLogoUrl(EncodeUtils.base64Encode(logoUrl));
                        goodsInfo.setGoodsSiftUrl(EncodeUtils.base64Encode(siftUrl));
                    }
                }
                returnMap.put("goodsList", goodsList);
                return Response.successResponse(returnMap);
        } catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsList fail", e);
            LOGGER.error("加载商品列表失败！");
            return Response.fail(BusinessErrorCode.LOAD_INFO_FAILED);
        }
    }


    /**
     * 加载商品列表(根据类目id查询商品):老方法，新方法请看:/loadGoodsListByCategoryId
     *
     * @return
     */
    @POST
    @Path("/loadGoodsListByCategoryId2")
    public Response loadGoodsListByCategoryId2(Map<String, Object> paramMap) {
        try {
            Map<String, Object> returnMap = new HashMap<String, Object>();

            String categoryId = CommonUtils.getValue(paramMap, "categoryId");// 类目Id
            String sort = CommonUtils.getValue(paramMap, "sort");// 排序字段
            String order = CommonUtils.getValue(paramMap, "order");// 顺序(desc（降序），asc（升序）)
            String page = CommonUtils.getValue(paramMap, "page");
            String rows = CommonUtils.getValue(paramMap, "rows");
            if (StringUtils.isEmpty(categoryId)) {
                LOGGER.error("类目id不能空！");
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
            if (StringUtils.isEmpty(order)) {
                order = "DESC";// 降序
            }
            Category cy = categoryInfoService.selectNameById(Long.parseLong(categoryId));
            Long level = cy.getLevel();
            GoodsBasicInfoEntity goodsInfoEntity = new GoodsBasicInfoEntity();
            if ("1".equals(level.toString())) {
                goodsInfoEntity.setCategoryId1(Long.parseLong(categoryId));
            } else if ("2".equals(level.toString())) {
                goodsInfoEntity.setCategoryId2(Long.parseLong(categoryId));
            } else if ("3".equals(level.toString())) {
                goodsInfoEntity.setCategoryId3(Long.parseLong(categoryId));
            }

            List<GoodsBasicInfoEntity> goodsBasicInfoList = null;
            Boolean falgePrice = false;
            if (CategorySort.CATEGORY_SortA.getCode().equals(sort)) {// 销量
                goodsInfoEntity.setSort("amount");
                goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
            } else if (CategorySort.CATEGORY_SortN.getCode().equals(sort)) {// 新品(商品的创建时间)
                goodsInfoEntity.setSort("new");
                goodsInfoEntity.setOrder(order);// 升序或降序
                goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
            } else if (CategorySort.CATEGORY_SortP.getCode().equals(sort)) {// 价格
                falgePrice = true;
                goodsInfoEntity.setSort("price");
                goodsInfoEntity.setOrder(order);// 升序或降序
                goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
            } else {// 默认（商品上架时间降序）
                goodsInfoEntity.setSort("default");
                goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
            }

            Integer totalCount = goodsService.loadGoodsByParamCount(goodsInfoEntity);
            returnMap.put("totalCount", totalCount);

            for (GoodsBasicInfoEntity goodsInfo : goodsBasicInfoList) {
                if (null != goodsInfo.getGoodId() && null != goodsInfo.getGoodsStockId()) {
                    ActivityInfoEntity param = new ActivityInfoEntity();
                    param.setGoodsId(goodsInfo.getGoodId());
                    param.setStatus(ActivityInfoStatus.EFFECTIVE.getCode());
                    List<ActivityInfoEntity> activitys = actityInfoDao.filter(param);
                    Map<String, Object> result = new HashMap<>();
                    if (null != activitys && activitys.size() > 0) {
                        result = goodService.getMinPriceGoods(goodsInfo.getGoodId());
                        BigDecimal price = (BigDecimal) result.get("minPrice");
                        Long minPriceStockId = (Long) result.get("minPriceStockId");
                        goodsInfo.setGoodsPrice(price);
                        goodsInfo.setGoodsPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2,
                                BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
                        goodsInfo.setGoodsStockId(minPriceStockId);
                    } else {
                        BigDecimal price = commonService.calculateGoodsPrice(goodsInfo.getGoodId(),
                                goodsInfo.getGoodsStockId());
                        goodsInfo.setGoodsPrice(price);
                        goodsInfo.setGoodsPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2,
                                BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
                    }

                    if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {// 京东图片
                        String logoUrl = goodsInfo.getGoodsLogoUrl();
                        goodsInfo.setGoodsLogoUrlNew("http://img13.360buyimg.com/n1/" + logoUrl);
                        goodsInfo.setGoodsLogoUrl("http://img13.360buyimg.com/n1/" + logoUrl);
                    } else {
                        Long marketPrice = goodsStockInfoRepository.getMaxMarketPriceByGoodsId(goodsInfo
                                .getGoodId());
                        goodsInfo.setMarketPrice(new BigDecimal(marketPrice));

                        String logoUrl = goodsInfo.getGoodsLogoUrl();
                        String siftUrl = goodsInfo.getGoodsSiftUrl();

                        goodsInfo.setGoodsLogoUrlNew(imageService.getImageUrl(logoUrl));
                        goodsInfo.setGoodsLogoUrl(EncodeUtils.base64Encode(logoUrl));
                        goodsInfo.setGoodsSiftUrlNew(imageService.getImageUrl(siftUrl));
                        goodsInfo.setGoodsSiftUrl(EncodeUtils.base64Encode(siftUrl));
                    }

                }
            }
            if (falgePrice && "DESC".equalsIgnoreCase(order)) {// 按售价排序(降序)
                GoodsBasicInfoEntity temp = new GoodsBasicInfoEntity();
                for (int i = 0; i < goodsBasicInfoList.size() - 1; i++) {
                    for (int j = i + 1; j < goodsBasicInfoList.size(); j++) {
                        if (goodsBasicInfoList.get(i).getGoodsPrice()
                                .compareTo(goodsBasicInfoList.get(j).getGoodsPrice()) < 0) {
                            temp = goodsBasicInfoList.get(i);
                            goodsBasicInfoList.set(i, goodsBasicInfoList.get(j));
                            goodsBasicInfoList.set(j, temp);
                        }
                    }
                }
            } else if (falgePrice) {
                GoodsBasicInfoEntity temp = new GoodsBasicInfoEntity();
                for (int i = 0; i < goodsBasicInfoList.size() - 1; i++) {
                    for (int j = i + 1; j < goodsBasicInfoList.size(); j++) {
                        if (goodsBasicInfoList.get(j).getGoodsPrice()
                                .compareTo(goodsBasicInfoList.get(i).getGoodsPrice()) < 0) {
                            temp = goodsBasicInfoList.get(i);
                            goodsBasicInfoList.set(i, goodsBasicInfoList.get(j));
                            goodsBasicInfoList.set(j, temp);
                        }
                    }
                }
            }
            returnMap.put("goodsList", goodsBasicInfoList);
            return Response.successResponse(returnMap);
        } catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsList fail", e);
            LOGGER.error("加载商品列表失败！");
            return Response.fail(BusinessErrorCode.LOAD_INFO_FAILED);
        }
    }
    /**
     * 通过ES加载商品列表(根据类目id查询商品)
     *
     * @return
     */
    @POST
    @Path("/loadGoodsListByCategoryId")
    public Response loadGoodsListByCategoryId(Map<String, Object> paramMap) {
        try {
            Map<String, Object> returnMap = new HashMap<String, Object>();
			GoodsSearchCondition goodsSearchCondition = new GoodsSearchCondition();

            String categoryId = CommonUtils.getValue(paramMap, "categoryId");// 类目Id
            String sort = CommonUtils.getValue(paramMap, "sort");// 排序字段
            String order = CommonUtils.getValue(paramMap, "order");// 顺序(desc（降序），asc（升序）)
            String page = CommonUtils.getValue(paramMap, "page");
            String rows = CommonUtils.getValue(paramMap, "rows");
            if (StringUtils.isEmpty(categoryId)) {
                LOGGER.error("类目id不能空！");
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
            if (!StringUtils.equalsIgnoreCase("ASC", order) && !StringUtils.equalsIgnoreCase("DESC", order)) {
				order = "DESC";// 降序
			}
            if (CategorySort.CATEGORY_SortA.getCode().equals(sort)) {
				if (StringUtils.equalsIgnoreCase("DESC", order)) {
					goodsSearchCondition.setSortMode(SortMode.SALEVALUE_DESC);
				} else {
					goodsSearchCondition.setSortMode(SortMode.SALEVALUE_ASC);
				}
			} else if (CategorySort.CATEGORY_SortN.getCode().equals(sort)) {// 新品(商品的创建时间)
				if (StringUtils.equalsIgnoreCase("DESC", order)) {
					goodsSearchCondition.setSortMode(SortMode.TIMECREATED_DESC);
				} else {
					goodsSearchCondition.setSortMode(SortMode.TIMECREATED_ASC);
				}
			} else if (CategorySort.CATEGORY_SortP.getCode().equals(sort)) {// 价格
				if (StringUtils.equalsIgnoreCase("DESC", order)) {
					goodsSearchCondition.setSortMode(SortMode.PRICE_DESC);
				} else {
					goodsSearchCondition.setSortMode(SortMode.PRICE_ASC);
				}
			} else {
				if (StringUtils.equalsIgnoreCase("DESC", order)) {
					goodsSearchCondition.setSortMode(SortMode.ORDERVALUE_DESC);
				} else {
					goodsSearchCondition.setSortMode(SortMode.ORDERVALUE_ASC);
				}
			}
            Integer pages = null;
			Integer row = null;
			if (StringUtils.isNotEmpty(rows)) {
				row = Integer.valueOf(rows);
			} else {
				row = 20;
			}
			pages = StringUtils.isEmpty(page) ? 1 : Integer.valueOf(page);
			//查询ES
			Pagination<Goods> pagination = new Pagination<>();
			pagination=IndexManager.goodSearchCategoryId2ForOtherCategory(categoryId, SortMode.SORD_ASC.getSortField(),goodsSearchCondition.getSortMode().getSortField(), goodsSearchCondition.getSortMode().isDesc(),(pages - 1) * row, row);
			//当es查询不到结果时，到数据库查询
			if(null==pagination || pagination.getDataList().size()==0){
				returnMap= loadGoodsListByCategoryIdByMysql(paramMap);
			} else {
				List<GoodsVo> list = new ArrayList<GoodsVo>();
				for (Goods goods : pagination.getDataList()) {
					list.add(goodsToGoodVo(goods));
					
				}
				returnMap.put("totalCount", pagination.getTotalCount());
				returnMap.put("goodsList", list);
			}
           return Response.successResponse(returnMap);
        } catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsList fail", e);
            LOGGER.error("加载商品列表失败！");
            return Response.fail(BusinessErrorCode.LOAD_INFO_FAILED);
        }
    }
    /**
     * 当ES报错或查不到数据时，从数据库查询
     * @return
     * @throws BusinessException
     */
	public Map<String, Object> loadGoodsListByCategoryIdByMysql(Map<String, Object> paramMap) throws BusinessException {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		String categoryId = CommonUtils.getValue(paramMap, "categoryId");// 类目Id
		String sort = CommonUtils.getValue(paramMap, "sort");// 排序字段
		String order = CommonUtils.getValue(paramMap, "order");// 顺序(desc（降序），asc（升序）)
		String page = CommonUtils.getValue(paramMap, "page");
		String rows = CommonUtils.getValue(paramMap, "rows");

		if (StringUtils.isEmpty(order)) {
			order = "DESC";// 降序
		}
		Category cy = categoryInfoService.selectNameById(Long.parseLong(categoryId));
		Long level = cy.getLevel();
		GoodsBasicInfoEntity goodsInfoEntity = new GoodsBasicInfoEntity();
		if ("1".equals(level.toString())) {
			goodsInfoEntity.setCategoryId1(Long.parseLong(categoryId));
		} else if ("2".equals(level.toString())) {
			goodsInfoEntity.setCategoryId2(Long.parseLong(categoryId));
		} else if ("3".equals(level.toString())) {
			goodsInfoEntity.setCategoryId3(Long.parseLong(categoryId));
		}

		List<GoodsBasicInfoEntity> goodsBasicInfoList = null;
		Boolean falgePrice = false;
		if (CategorySort.CATEGORY_SortA.getCode().equals(sort)) {// 销量
			goodsInfoEntity.setSort("amount");
			goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
		} else if (CategorySort.CATEGORY_SortN.getCode().equals(sort)) {// 新品(商品的创建时间)
			goodsInfoEntity.setSort("new");
			goodsInfoEntity.setOrder(order);// 升序或降序
			goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
		} else if (CategorySort.CATEGORY_SortP.getCode().equals(sort)) {// 价格
			falgePrice = true;
			goodsInfoEntity.setSort("price");
			goodsInfoEntity.setOrder(order);// 升序或降序
			goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
		} else {// 默认（商品上架时间降序）
			goodsInfoEntity.setSort("default");
			goodsBasicInfoList = goodsService.loadGoodsByParam(goodsInfoEntity, page, rows);
		}

		Integer totalCount = goodsService.loadGoodsByParamCount(goodsInfoEntity);
		returnMap.put("totalCount", totalCount);

		for (GoodsBasicInfoEntity goodsInfo : goodsBasicInfoList) {
			if (null != goodsInfo.getGoodId() && null != goodsInfo.getGoodsStockId()) {
				ActivityInfoEntity param = new ActivityInfoEntity();
				param.setGoodsId(goodsInfo.getGoodId());
				param.setStatus(ActivityInfoStatus.EFFECTIVE.getCode());
				List<ActivityInfoEntity> activitys = actityInfoDao.filter(param);
				Map<String, Object> result = new HashMap<>();
				if (null != activitys && activitys.size() > 0) {
					result = goodService.getMinPriceGoods(goodsInfo.getGoodId());
					BigDecimal price = (BigDecimal) result.get("minPrice");
					Long minPriceStockId = (Long) result.get("minPriceStockId");
					goodsInfo.setGoodsPrice(price);
					goodsInfo.setGoodsPriceFirst(
							(new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
					goodsInfo.setGoodsStockId(minPriceStockId);
				} else {
					BigDecimal price = commonService.calculateGoodsPrice(goodsInfo.getGoodId(),
							goodsInfo.getGoodsStockId());
					goodsInfo.setGoodsPrice(price);
					goodsInfo.setGoodsPriceFirst(
							(new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));// 设置首付价=商品价*10%
				}

				if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {// 京东图片
					String logoUrl = goodsInfo.getGoodsLogoUrl();
					goodsInfo.setGoodsLogoUrlNew("http://img13.360buyimg.com/n1/" + logoUrl);
					goodsInfo.setGoodsLogoUrl("http://img13.360buyimg.com/n1/" + logoUrl);
				} else {
					Long marketPrice = goodsStockInfoRepository.getMaxMarketPriceByGoodsId(goodsInfo.getGoodId());
					goodsInfo.setMarketPrice(new BigDecimal(marketPrice));

					String logoUrl = goodsInfo.getGoodsLogoUrl();
					String siftUrl = goodsInfo.getGoodsSiftUrl();

					goodsInfo.setGoodsLogoUrlNew(imageService.getImageUrl(logoUrl));
					goodsInfo.setGoodsLogoUrl(EncodeUtils.base64Encode(logoUrl));
					goodsInfo.setGoodsSiftUrlNew(imageService.getImageUrl(siftUrl));
					goodsInfo.setGoodsSiftUrl(EncodeUtils.base64Encode(siftUrl));
				}

			}
		}
		if (falgePrice && "DESC".equalsIgnoreCase(order)) {// 按售价排序(降序)
			GoodsBasicInfoEntity temp = new GoodsBasicInfoEntity();
			for (int i = 0; i < goodsBasicInfoList.size() - 1; i++) {
				for (int j = i + 1; j < goodsBasicInfoList.size(); j++) {
					if (goodsBasicInfoList.get(i).getGoodsPrice()
							.compareTo(goodsBasicInfoList.get(j).getGoodsPrice()) < 0) {
						temp = goodsBasicInfoList.get(i);
						goodsBasicInfoList.set(i, goodsBasicInfoList.get(j));
						goodsBasicInfoList.set(j, temp);
					}
				}
			}
		} else if (falgePrice) {
			GoodsBasicInfoEntity temp = new GoodsBasicInfoEntity();
			for (int i = 0; i < goodsBasicInfoList.size() - 1; i++) {
				for (int j = i + 1; j < goodsBasicInfoList.size(); j++) {
					if (goodsBasicInfoList.get(j).getGoodsPrice()
							.compareTo(goodsBasicInfoList.get(i).getGoodsPrice()) < 0) {
						temp = goodsBasicInfoList.get(i);
						goodsBasicInfoList.set(i, goodsBasicInfoList.get(j));
						goodsBasicInfoList.set(j, temp);
					}
				}
			}
		}
		returnMap.put("goodsList", goodsBasicInfoList);
		return returnMap;
	}
    /**
     * 获取商品详细信息 基本信息+详细信息(规格 价格 剩余量)
     *
     * @return
     */
    @POST
    @Path("/loadDetailInfoById")
    public Response loadGoodsBasicInfo(Map<String, Object> paramMap){
        try{
            Map<String,Object> returnMap = new HashMap<>();
            Long goodsId = CommonUtils.getLong(paramMap,"goodsId");
            String userId = CommonUtils.getValue(paramMap, "userId");
            if(null==goodsId){
            	LOGGER.error("商品号不能为空!");
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
            if (!StringUtils.isEmpty(userId)) {
                int amountInCart = shoppingCartService.getNumOfTypeInCart(userId);
                returnMap.put("amountInCart", amountInCart);
            }
            goodService.loadGoodsBasicInfoById(goodsId,returnMap);
            return Response.success("加载成功", returnMap);
        } catch (BusinessException e) {
            LOGGER.error("ShopHomeController loadGoodsBasicInfo fail", e);
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        }
        catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsBasicInfo fail", e);
            LOGGER.error("获取商品基本信息失败");
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        }
    }

    /**
     * 获取商品详细信息 基本信息+详细信息(规格 价格 剩余量)
     *
     * @return
     */
    @POST
    @Path("/v2/loadDetailInfoById")
    public Response loadGoodsBasicInfoJD(Map<String, Object> paramMap) {
        LOGGER.info("获取商品详细信息 基本信息+详细信息,v2 loadGoodsBasicInfo方法开始执行,参数:{}",GsonUtils.toJson(paramMap));
        try {
            Map<String, Object> returnMap = new HashMap<>();
            Long goodsId = CommonUtils.getLong(paramMap, "goodsId");
            String userId = CommonUtils.getValue(paramMap, "userId");
            String provinceCode = CommonUtils.getValue(paramMap, "provinceCode");
            String cityCode = CommonUtils.getValue(paramMap, "cityCode");
            String districtCode = CommonUtils.getValue(paramMap, "districtCode");
            String townsCode = CommonUtils.getValue(paramMap, "townsCode");

            if (null == goodsId) {
                LOGGER.error("商品号不能为空!");
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
            Boolean flage = true;
            Region region = new Region();// app端传过来的地址
            if (!StringUtils.isAnyEmpty(provinceCode, cityCode, districtCode)) {
                    List<DictDTO> result = nationService.queryDistrictJd(districtCode);
                    if (result.size() == 0) {
                        region.setProvinceId(Integer.parseInt(provinceCode));
                        region.setCityId(Integer.parseInt(cityCode));
                        region.setCountyId(Integer.parseInt(districtCode));
                        region.setTownId(0);
                        flage = false;
                    } else {
                        if (StringUtils.isNotEmpty(townsCode)) {
                            region.setProvinceId(Integer.parseInt(provinceCode));
                            region.setCityId(Integer.parseInt(cityCode));
                            region.setCountyId(Integer.parseInt(districtCode));
                            region.setTownId(StringUtils.isEmpty(townsCode) ? 0 : Integer.parseInt(townsCode));
                            flage = false;
                        } else {
                            return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
                        }
                    }
            }
            Region region2 = new Region();
            // 查看地址信息
            AddressInfoEntity addty = new AddressInfoEntity();
            // 查询京东地址
            List<AddressInfoEntity> addressInfoList = new ArrayList<>();
            if (StringUtils.isNotEmpty(userId)) {
                addressInfoList = addressService.queryAddressInfoJd(Long.valueOf(userId));
            }
            if (addressInfoList.size() > 0) {
                if (!("1".equals(addressInfoList.get(0).getIsDefault()))) {
                    addressInfoList.get(0).setIsDefault("1");
                }
            }
            // app端没传地址并且数据库地址为空时，使用默认地址
            if (flage && addressInfoList.size() == 0) {
                addty.setId(Long.parseLong("-1"));
                addty.setProvinceCode("2");
                addty.setProvince("上海");
                addty.setCityCode("2815");
                addty.setCity("长宁区");
                addty.setDistrictCode("51975");
                addty.setDistrict("城区");
                addty.setTownsCode("0");
                addty.setTowns("");
                addty.setIsDefault("1");
                addressInfoList.add(addty);
            }
            // 获取地址信息
            for (AddressInfoEntity addressInfoEntity : addressInfoList) {
                if ("1".equals(addressInfoEntity.getIsDefault())) {
                    region2.setProvinceId(Integer.parseInt(addressInfoEntity.getProvinceCode()));
                    region2.setCityId(Integer.parseInt(addressInfoEntity.getCityCode()));
                    region2.setCountyId(Integer.parseInt(addressInfoEntity.getDistrictCode()));
                    region2.setTownId(StringUtils.isEmpty(addressInfoEntity.getTownsCode()) ? 0 : Integer
                            .parseInt(addressInfoEntity.getTownsCode()));
                }
            }
            Region region3 = new Region();
            if (flage) {
                region3 = region2;
            } else {
                region3 = region;
            }
            GoodsInfoEntity goodsInfo = goodsService.selectByGoodsId(Long.valueOf(goodsId));
            // 判断是否是京东商品
            if (SourceType.WZ.getCode().equals(goodsInfo.getSource())) {// 来源于京东
                String externalId = goodsInfo.getExternalId();// 外部商品id
                if(!orderService.checkGoodsSalesOrNot(externalId)){
                	goodsInfo.setStatus("G03");//商品下架
                }
                returnMap = jdGoodsInfoService.getAppJdGoodsAllInfoBySku(
                        Long.valueOf(externalId).longValue(), goodsId.toString(),region3);
                
                //添加活动id
            	ProGroupGoodsBo proGroupGoodsBo=proGroupGoodsService.getByGoodsId(goodsId);
            	if(null !=proGroupGoodsBo && proGroupGoodsBo.isValidActivity()){
            	    returnMap.put("proActivityId",proGroupGoodsBo.getActivityId());
            	}
            	//获取商品的优惠券
            	List<String> proCoupons=jdGoodsInfoService.getProCouponList(goodsId);
            	if(proCoupons.size()>3){
            		returnMap.put("proCouponList",proCoupons.subList(0, 3));
            	}else{
            		 returnMap.put("proCouponList",proCoupons); 
            	}
                returnMap.put("goodsName", goodsInfo.getGoodsName());// 商品名称
                returnMap.put("merchantCode", goodsInfo.getMerchantCode());// 商户编码
                returnMap.put("activityCfg", goodsService.getActivityInfo(goodsId));// 满减活动字段
                returnMap.put("support7dRefund",goodsService.getsupport7dRefund(Long.parseLong(externalId)));// 是否支持7天无理由退货,Y、N
                List<GoodsStockInfoEntity> jdGoodsStockInfoList = goodsStockInfoRepository
                        .loadByGoodsId(goodsId);
                if (jdGoodsStockInfoList.size() == 1) {
                    BigDecimal price = commonService.calculateGoodsPrice(goodsId, jdGoodsStockInfoList.get(0)
                            .getId());
                    returnMap.put("goodsPrice", price);// 商品价格
                    returnMap.put("goodsPriceFirstPayment",
                            (new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));// 商品首付价格
                    // 京东商品没有规格情况拼凑数据格式
                    int jdSimilarSkuListSize = (int) returnMap.get("jdSimilarSkuListSize");
                    if (jdSimilarSkuListSize == 0) {
                        List<JdSimilarSkuTo> JdSimilarSkuToList = (List<JdSimilarSkuTo>) returnMap
                                .get("JdSimilarSkuToList");
                        JdSimilarSkuTo jdSimilarSkuTo = new JdSimilarSkuTo();
                        JdSimilarSkuVo jdSimilarSkuVo = new JdSimilarSkuVo();
                        jdSimilarSkuVo.setGoodsId(goodsId.toString());
                        jdSimilarSkuVo.setSkuId(externalId);
                        jdSimilarSkuVo.setGoodsStockId(jdGoodsStockInfoList.get(0).getId().toString());
                        jdSimilarSkuVo.setPrice(price);
                        jdSimilarSkuVo.setPriceFirst((new BigDecimal("0.1").multiply(price)).setScale(2,
                                BigDecimal.ROUND_DOWN));
                        jdSimilarSkuVo.setStockDesc(returnMap.get("goodsStockDes").toString());
                        jdSimilarSkuTo.setSkuIdOrder("");
                        jdSimilarSkuTo.setJdSimilarSkuVo(jdSimilarSkuVo);
                        JdSimilarSkuToList.add(jdSimilarSkuTo);
                    }
                }
                returnMap.put("source", "jd");
                returnMap.put("goodsTitle", goodsInfo.getGoodsTitle());
                returnMap.put("status", goodsInfo.getStatus());
            } else {
                goodService.loadGoodsBasicInfoById(goodsId, returnMap);
            }
            // 获取购物车中商品种类数
            if (!StringUtils.isEmpty(userId)) {
                int amountInCart = shoppingCartService.getNumOfTypeInCart(userId);
                returnMap.put("amountInCart", amountInCart);
            }
            returnMap.put("addressList", addressInfoList);
            return Response.success("加载成功", returnMap);
        } catch (BusinessException e) {
            LOGGER.error("ShopHomeController loadGoodsBasicInfo fail", e);
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        } catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsBasicInfo fail", e);
            LOGGER.error("获取商品基本信息失败");
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        }
    }
    /**
     * 获取商品详细信息 基本信息+详细信息(规格 价格 剩余量)（sprint 11）
     * 非京东商品也变成多规格商品
     * @return
     */
	@POST
    @Path("/v3/loadDetailInfoById")
    public Response loadGoodsBasicInfoJD2(Map<String, Object> paramMap) {
        try {
            Map<String, Object> returnMap = new HashMap<>();
            Long goodsId = CommonUtils.getLong(paramMap, "goodsId");
            String userId = CommonUtils.getValue(paramMap, "userId");
            String provinceCode = CommonUtils.getValue(paramMap, "provinceCode");
            String cityCode = CommonUtils.getValue(paramMap, "cityCode");
            String districtCode = CommonUtils.getValue(paramMap, "districtCode");
            String townsCode = CommonUtils.getValue(paramMap, "townsCode");
            if (null == goodsId) {
                LOGGER.error("商品号不能为空!");
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
            Region region = null;// app端传过来的地址
            if (!StringUtils.isAnyEmpty(provinceCode, cityCode, districtCode)) {
                region = new Region();
                List<DictDTO> result = nationService.queryDistrictJd(districtCode);
                region.setProvinceId(Integer.parseInt(provinceCode));
                region.setCityId(Integer.parseInt(cityCode));
                region.setCountyId(Integer.parseInt(districtCode));
                if (result.size() != 0 && StringUtils.isEmpty(townsCode)) {
                    return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
                }
                region.setTownId(StringUtils.isEmpty(townsCode) ? 0 : Integer.parseInt(townsCode));
            }
            // 如果flage = false则地址用前端传递过来的，否则先去查询数据库是否有改用户的地址信息，如果没有再使用平台默认地址信息
            // 查询京东地址
            List<AddressInfoEntity> addressInfoList = new ArrayList<>();
            if (StringUtils.isNotEmpty(userId)) {
                addressInfoList = addressService.queryAddressInfoJd(Long.valueOf(userId));
            }
            if (addressInfoList.size() > 0) {
                if (!("1".equals(addressInfoList.get(0).getIsDefault()))) {
                    addressInfoList.get(0).setIsDefault("1");
                }
            }
            if (null == region) {
                region = new Region();
                if (addressInfoList.size() == 0) {
                    AddressInfoEntity addty = new AddressInfoEntity();
                    addty.setId(Long.parseLong("-1"));
                    addty.setProvinceCode("2");
                    addty.setProvince("上海");
                    addty.setCityCode("2815");
                    addty.setCity("长宁区");
                    addty.setDistrictCode("51975");
                    addty.setDistrict("城区");
                    addty.setTownsCode("0");
                    addty.setTowns("");
                    addressInfoList.add(addty);
                }
                addressInfoList.get(0).setIsDefault("1");
                region.setProvinceId(Integer.parseInt(addressInfoList.get(0).getProvinceCode()));
                region.setCityId(Integer.parseInt(addressInfoList.get(0).getCityCode()));
                region.setCountyId(Integer.parseInt(addressInfoList.get(0).getDistrictCode()));
                region.setTownId(StringUtils.isEmpty(addressInfoList.get(0).getTownsCode()) ? 0
                        : Integer.parseInt(addressInfoList.get(0).getTownsCode()));
            }
            GoodsInfoEntity goodsInfo = goodsService.selectByGoodsId(Long.valueOf(goodsId));
            if (null == goodsInfo) {
                LOGGER.error("商品信息不存在:{}", goodsId);
                throw new BusinessException("商品信息不存在");
            }
            //对所有的（wz，jd，非第三方）进行校验
            Date now = new Date();
            if (now.before(goodsInfo.getListTime())
                    || (null != goodsInfo.getDelistTime() && now.after(goodsInfo.getDelistTime()))
                    || !GoodStatus.GOOD_UP.getCode().equals(goodsInfo.getStatus())) {
                goodsInfo.setStatus(GoodStatus.GOOD_DOWN.getCode());
            }
            
            // 商品规格
            List<GoodsStockInfoEntity> jdGoodsStockInfoList = goodsStockInfoRepository.loadByGoodsId(goodsId);
            //价格计算
            if (jdGoodsStockInfoList.size() == 1) {
                BigDecimal price = commonService.calculateGoodsPrice(goodsId, jdGoodsStockInfoList.get(0).getId());
                goodsInfo.setGoodsPrice(price);
                goodsInfo.setFirstPrice((new BigDecimal("0.1").multiply(price)).setScale(2, BigDecimal.ROUND_DOWN));
            }
            if (SourceType.JD.getCode().equals(goodsInfo.getSource())
                    || SourceType.WZ.getCode().equals(goodsInfo.getSource())) {
                String externalId = goodsInfo.getExternalId();// 外部商品id

                if (SourceType.JD.getCode().equals(goodsInfo.getSource())) {
                    returnMap = jdGoodsInfoService.getAppJdGoodsAllInfoBySku(Long.valueOf(externalId).longValue(),
                            goodsId.toString(), region);
                    returnMap.put("source", SourceType.JD.getCode());
                    returnMap.put("status", GoodStatus.GOOD_DOWN.getCode());
                } else {
                    returnMap = weiZhiGoodsInfoService.getAppWzGoodsAllInfoBySku(Long.valueOf(externalId).longValue(),
                            goodsId.toString(), region);
                    returnMap.put("source", SourceType.JD.getCode());
                    returnMap.put("status", goodsInfo.getStatus());
                    // 验证商品是否可售（当验证为不可售时，更新数据库商品状态）
                    if (StringUtils.isNotBlank(externalId) && !orderService.checkGoodsSalesOrNot(externalId)) {
                        returnMap.put("status",GoodStatus.GOOD_DOWN.getCode());// 商品下架
                    }
                }
                // 是否支持7天无理由退货,Y、N
                returnMap.put("support7dRefund", goodsService.getsupport7dRefund(Long.parseLong(externalId)));
                returnMap.put("goodsPrice", goodsInfo.getGoodsPrice());// 商品价格
                returnMap.put("goodsPriceFirstPayment",goodsInfo.getFirstPrice());// 商品首付价格
            } else {
            	Boolean isUnSupport=false;
            	WorkCityJd workCityJd=nationService.selectByProvinceId(region.getProvinceId()+"");
            	if (StringUtils.isNotBlank(goodsInfo.getUnSupportProvince()) && null !=workCityJd
                        && goodsInfo.getUnSupportProvince().indexOf(workCityJd.getProvince()) > -1) {
            		isUnSupport = true;
               }
                returnMap.put("status", goodsInfo.getStatus());// 商品下架
            	returnMap.put("isUnSupport", isUnSupport);
            	returnMap.put("userId", userId);
                goodService.loadGoodsBasicInfoById2(goodsId, returnMap);//sprint11(商品多规格)
            }
            // 京东商品没有规格情况拼凑数据格式
            int jdSimilarSkuListSize = (int) returnMap.get("jdSimilarSkuListSize");
            List<JdSimilarSku> jdSimilarSkuList = (List<JdSimilarSku>) returnMap.get("jdSimilarSkuList");
            if (jdSimilarSkuListSize == 0 || null ==jdSimilarSkuList || jdSimilarSkuList.isEmpty()) {
                List<JdSimilarSkuTo> JdSimilarSkuToList = (List<JdSimilarSkuTo>) returnMap.get("JdSimilarSkuToList");
                JdSimilarSkuTo jdSimilarSkuTo = new JdSimilarSkuTo();
                JdSimilarSkuVo jdSimilarSkuVo = new JdSimilarSkuVo();
              //根据skuId查询该规格是否参加了限时购活动
				LimitGoodsSkuVo limitGS;
                String source =(String) returnMap.get("source");
                if(StringUtils.equals(SourceType.JD.getCode(), source)){
                	 jdSimilarSkuVo.setSkuId(goodsInfo.getExternalId());
                     limitGS=limitCommonService.selectLimitByGoodsId(userId,goodsInfo.getExternalId());
                }else{
                	jdSimilarSkuVo.setSkuId(jdGoodsStockInfoList.get(0).getSkuId());
                	jdSimilarSkuVo.setStockCurrAmt(jdGoodsStockInfoList.get(0).getStockCurrAmt());
                    limitGS=limitCommonService.selectLimitByGoodsId(userId,jdGoodsStockInfoList.get(0).getSkuId());
                }
				if(null !=limitGS){
				BigDecimal limitActivityPrice=limitGS.getActivityPrice();
				limitActivityPrice.setScale(2, BigDecimal.ROUND_DOWN);
				jdSimilarSkuVo.setPrice(goodsInfo.getGoodsPrice());//限时购活动价
				jdSimilarSkuVo.setPriceFirst(goodsInfo.getFirstPrice());
				jdSimilarSkuVo.setLimitActivityPrice(limitActivityPrice);
				jdSimilarSkuVo.setLimitActivityPriceFirst((new BigDecimal("0.1").multiply(limitActivityPrice)).setScale(2, BigDecimal.ROUND_DOWN));
				jdSimilarSkuVo.setIsLimitActivity(true);
				jdSimilarSkuVo.setLimitNum(limitGS.getLimitNum());
				jdSimilarSkuVo.setLimitPersonNum(limitGS.getLimitPersonNum());
				jdSimilarSkuVo.setLimitBuyActId(limitGS.getLimitBuyActId());
				jdSimilarSkuVo.setLimitBuyFalg(limitGS.getLimitFalg());
				jdSimilarSkuVo.setLimitBuyTime(limitGS.getTime());
				jdSimilarSkuVo.setLimitBuyStartTime(limitGS.getStartTime());
				jdSimilarSkuVo.setLimitBuyEndTime(limitGS.getEndTime());
				}else{
		          jdSimilarSkuVo.setPrice(goodsInfo.getGoodsPrice());
                  jdSimilarSkuVo.setPriceFirst(goodsInfo.getFirstPrice());
				}
                jdSimilarSkuVo.setGoodsId(goodsId.toString());
                jdSimilarSkuVo.setGoodsStockId(jdGoodsStockInfoList.get(0).getId().toString());
                jdSimilarSkuVo.setStockDesc(returnMap.get("goodsStockDes").toString());
                jdSimilarSkuTo.setSkuIdOrder("");
                jdSimilarSkuTo.setJdSimilarSkuVo(jdSimilarSkuVo);
                JdSimilarSkuToList.add(jdSimilarSkuTo);
            }
            // 添加活动id
            ProGroupGoodsBo proGroupGoodsBo = proGroupGoodsService.getByGoodsId(goodsId);
            if (null != proGroupGoodsBo && proGroupGoodsBo.isValidActivity()) {
                returnMap.put("proActivityId", proGroupGoodsBo.getActivityId());
            }
            // 获取商品的优惠券
            List<String> proCoupons = jdGoodsInfoService.getProCouponList(goodsId);
            if (proCoupons.size() > 3) {
                returnMap.put("proCouponList", proCoupons.subList(0, 3));
            } else {
                returnMap.put("proCouponList", proCoupons);
            }
            // 商品名称
            returnMap.put("goodsName", goodsInfo.getGoodsName());
            // 商户编码
            returnMap.put("merchantCode", goodsInfo.getMerchantCode());
            // 满减活动字段
            returnMap.put("activityCfg", goodsService.getActivityInfo(goodsId));

            // 商品title
            returnMap.put("goodsTitle", goodsInfo.getGoodsTitle());
            // 获取购物车中商品种类数
            if (StringUtils.isNotBlank(userId)) {
                int amountInCart = shoppingCartService.getNumOfTypeInCart(userId);
                returnMap.put("amountInCart", amountInCart);
            }
            returnMap.put("addressList", addressInfoList);
            return Response.success("加载成功", returnMap);
        } catch (BusinessException e) {
            LOGGER.error("ShopHomeController loadGoodsBasicInfo fail", e);
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        } catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsBasicInfo fail", e);
            LOGGER.error("获取商品基本信息失败");
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        }
    }
    /**
     * 
     *获取商品优惠券列表
     * @return
     */
    @POST
    @Path("/v3/getProCouponsList")
    public Response getProCouponsList(Map<String, Object> paramMap) {
        Long goodsId = CommonUtils.getLong(paramMap, "goodsId");
        String userId = CommonUtils.getValue(paramMap, "userId");
        //获取商品的优惠券
        Map<String,Object>  returnMap=jdGoodsInfoService.getProCoupons(goodsId,Long.parseLong(userId));
        return Response.success("获取商品优惠券列表成功！", returnMap);
    }
    /**
     * 商品优惠券列表领取优惠券接口
     * @param paramMap
     * @return
     */
	@POST
    @Path("/v3/saveCoupon")
	public Response giveCouponToUser(Map<String, Object> paramMap){
        Long goodsId = CommonUtils.getLong(paramMap, "goodsId");
		String userId = CommonUtils.getValue(paramMap, "userId");
		String activityId = CommonUtils.getValue(paramMap, "activityId");
		String couponId = CommonUtils.getValue(paramMap, "couponId");
		if(StringUtils.isBlank(activityId)){
			LOGGER.error("活动编号不能为空!");
			return Response.fail("活动编号不能为空!");
		}
		LOGGER.info("getGroupAndGoodsByGroupId:--------->{}",GsonUtils.toJson(paramMap));
		try {
			int count = myCouponManagerService.giveCouponToUser(new MyCouponVo(Long.parseLong(userId),Long.parseLong(couponId),Long.parseLong(activityId)));
			if(count > 0){
		      //获取商品的优惠券
		      Map<String,Object>  returnMap=jdGoodsInfoService.getProCoupons(goodsId,Long.parseLong(userId));
		      LOGGER.info("giveCouponToUser:--------->{}",GsonUtils.toJson(returnMap));
			  return Response.success("领取成功!",returnMap);
			}
		} catch(BusinessException e){
			LOGGER.error("business giveCouponToUser :{}",e);
			return Response.fail(e.getErrorDesc());
		} catch (Exception e) {
			LOGGER.error("exception giveCouponToUser :{}",e);
		}
		return Response.fail("当前领券人数过多，请稍后再试!");
	}
    /**
     * 地址改变，查看是否有货
     *
     * @return
     */
    @POST
    @Path("/v2/addressChange")
    public Response addressChange(Map<String, Object> paramMap) {
        String goodsId = CommonUtils.getValue(paramMap, "goodsId");
        String provinceCode = CommonUtils.getValue(paramMap, "provinceCode");
        String cityCode = CommonUtils.getValue(paramMap, "cityCode");
        String districtCode = CommonUtils.getValue(paramMap, "districtCode");
        String townsCode = CommonUtils.getValue(paramMap, "townsCode");
        if (StringUtils.isAnyEmpty(goodsId, provinceCode, cityCode, districtCode)) {
            LOGGER.error("传入参数不能为空!");
            return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
        }
        if (!CityJdEnums.isContainsCode(provinceCode)) {
            try {
                ValidateUtils.isNotBlank(townsCode, "乡镇不能为空！");
            } catch (BusinessException e) {
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
        }
        Map<String, Object> map = new HashMap<>();
        GoodsInfoEntity goodsInfo = goodsService.selectByGoodsId(Long.valueOf(goodsId));
        if (null == goodsInfo) {
            return Response.fail(BusinessErrorCode.PARAM_VALUE_ERROR);
        }
        if (StringUtils.isBlank(townsCode)) {
            townsCode = "0";
        }
        String goodsStockDes = "有货";
        if ("jd".equals(goodsInfo.getSource())) {
            Region region = new Region();
            region.setProvinceId(Integer.parseInt(provinceCode));
            region.setCityId(Integer.parseInt(cityCode));
            region.setCountyId(Integer.parseInt(districtCode));
            region.setTownId(Integer.parseInt(townsCode));
            // 查询商品是否有货
            goodsStockDes = jdGoodsInfoService.getStockBySku(goodsInfo.getExternalId(), region);
            map.put("goodsStockDes", goodsStockDes);
        }
        return Response.success("成功！", map);
    }

    /**
     * 根据商品id获取商品规格详情信息(商品库存表)
     *
     * @return
     */
    @POST
    @Path("/loadGoodsStockByGoodsId")
    public Response loadGoodsStockInfo(Map<String, Object> paramMap) {
        try {
            Map<String, Object> returnMap = new HashMap<>();
            Long goodsId = CommonUtils.getLong(paramMap, "goodsId");
            if (null == goodsId) {
                LOGGER.error("商品号不能为空!");
                return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY);
            }
            List<GoodsStockInfoEntity> goodsStockList = goodService.loadDetailInfoByGoodsId(goodsId);
            for (GoodsStockInfoEntity goodsStock : goodsStockList) {
                BigDecimal price = commonService.calculateGoodsPrice(goodsStock.getGoodsId(),
                        goodsStock.getId());
                goodsStock.setGoodsPrice(price);
                String stockLogoUrl = goodsStock.getStockLogo();
                goodsStock.setStockLogoNew(imageService.getImageUrl(stockLogoUrl));
                goodsStock.setStockLogo(EncodeUtils.base64Encode(stockLogoUrl));
            }

            GoodsInfoEntity goodsInfo = goodService.selectByGoodsId(goodsId);
            if (null != goodsInfo) {
                returnMap.put("skyType", goodsInfo.getGoodsSkuType());
            }
            returnMap.put("goodsStockList", goodsStockList);
            return Response.success("加载成功", returnMap);
        } catch (Exception e) {
            LOGGER.error("ShopHomeController loadGoodsStockInfo fail", e);
            LOGGER.error("获取商品库存信息失败");
            return Response.fail(BusinessErrorCode.GET_INFO_FAILED);
        }
    }

    /**
     * 热买单品列表
     *
     * @param paramMap
     * @return
     */
    @POST
    @Path("/popularProducts")
    public Response popularProducts(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> list = goodsService.popularGoods(0, 50);
        List<GoodsInfoEntity> goodsList = new ArrayList<>();
        List<String> goodsIdList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list) || list.size() < 50) {
            if (CollectionUtils.isEmpty(list)) {
                goodsIdList = goodsService.getRemainderGoodsNew(0, 50);
            } else {
                goodsIdList = goodsService.getRemainderGoodsNew(0, 50 - list.size());
            }
            if (CollectionUtils.isNotEmpty(goodsIdList)) {
                // list.removeAll(goodsIdList);
                list.addAll(goodsIdList);
            }
        }
        try {
            goodsList = getSaleVolumeGoods(list);
            CategoryVo v = new CategoryVo();
            v.setCategoryTitle("大小家电 尽在掌握");
            v.setPictureUrl(espImageUrl + "/static/eshop/other/1502349644763.jpg");
            resultMap.put("banner", v);
            resultMap.put("goodsList", goodsList);
            if (CollectionUtils.isEmpty(goodsList)) {
                resultMap.put("totalCount", 0);
            } else {
                resultMap.put("totalCount", goodsList.size());
            }
        } catch (Exception e) {
            LOGGER.error("查询热买单品列表失败", e);
            return Response.fail(BusinessErrorCode.NO);
        }
        return Response.successResponse(resultMap);
    }

    /**
     * 获取商品价格
     *
     * @param goodsIds
     * @return
     * @throws BusinessException
     */
    private List<GoodsInfoEntity> getSaleVolumeGoods(List<String> goodsIds) throws BusinessException {
        List<GoodsInfoEntity> goodsList = new ArrayList<>();
        for (String goodsId : goodsIds) {
            GoodsInfoEntity goodsInfoEntity = goodsService.selectByGoodsId(Long.valueOf(goodsId));
            if (goodsInfoEntity == null) {
                LOGGER.error("热销商品id:{}在商品表中无对应商品", goodsId);
                throw new BusinessException("数据异常");
            }
            if (StringUtils.isEmpty(goodsInfoEntity.getSource())) {
                goodsInfoEntity
                        .setGoodsLogoUrlNew(imageService.getImageUrl(goodsInfoEntity.getGoodsLogoUrl()));// 非京东
                goodsInfoEntity
                        .setGoodsSiftUrlNew(imageService.getImageUrl(goodsInfoEntity.getGoodsSiftUrl()));
            } else {
                goodsInfoEntity.setGoodsLogoUrlNew("http://img13.360buyimg.com/n1/"
                        + goodsInfoEntity.getGoodsLogoUrl());
                goodsInfoEntity.setGoodsSiftUrlNew("http://img13.360buyimg.com/n1/"
                        + goodsInfoEntity.getGoodsSiftUrl());
                goodsInfoEntity.setSource("jd");
            }
            goodsInfoEntity.setGoogsDetail("");
            BigDecimal goodsPrice = getGoodsPrice(Long.valueOf(goodsId));
            if (goodsPrice != null) {
                goodsInfoEntity.setGoodsPrice(goodsPrice.setScale(2, BigDecimal.ROUND_FLOOR));
                goodsInfoEntity.setFirstPrice(goodsPrice.divide(new BigDecimal(10)).setScale(2,
                        BigDecimal.ROUND_FLOOR));
            }

            goodsList.add(goodsInfoEntity);
        }

        return goodsList;
    }

    /**
     * 必买清单
     *
     * @param paramMap
     * @return
     */
    @POST
    @Path("/crazeProducts")
    public Response crazeProducts(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Long pageIndex = CommonUtils.getLong(paramMap, "pageIndex");
        int pageSize = 20;
        if (pageIndex == null || pageIndex < 1) {
            return Response.fail(BusinessErrorCode.PARAM_VALUE_ERROR);
        }
        if (pageIndex.intValue() > 6) {
            resultMap.put("goodsNecessaryList", Collections.emptyList());
            return Response.successResponse(resultMap);
        }
        try {
            // 热卖单品
            List<String> list = goodsService.popularGoods(0, 4);
            List<String> goodsIdList = new ArrayList<>();
            if (CollectionUtils.isEmpty(list) || list.size() < 4) {
                if (CollectionUtils.isEmpty(list)) {
                    goodsIdList = goodsService.getRemainderGoodsNew(0, 4);
                } else {
                    goodsIdList = goodsService.getRemainderGoodsNew(0, 4 - list.size());
                }
                if (CollectionUtils.isNotEmpty(goodsIdList)) {
                    goodsIdList.addAll(list);
                }
            } else {
                goodsIdList = list;
            }
            List<GoodsInfoEntity> goodsPopuLists = new ArrayList<>();
            goodsPopuLists = getSaleVolumeGoods(goodsIdList);
            resultMap.put("goodsPopuLists", goodsPopuLists);
            // 必买清单
            Pagination<String> jdGoodSalesVolumePagination = goodsService.jdGoodSalesVolume(
                    pageIndex.intValue(), pageSize);
            List<GoodsInfoEntity> goodsNecessaryList = new ArrayList<>();
            List<String> goodsNcessids = jdGoodSalesVolumePagination.getDataList();
            goodsNecessaryList = getSaleVolumeGoods(goodsNcessids);
            resultMap.put("goodsNecessaryList", goodsNecessaryList);
        } catch (BusinessException e) {
            LOGGER.error("查询首页推荐列表失败", e);
            return Response.fail(e.getErrorDesc());
        }
        return Response.successResponse(resultMap);
    }

    /**
     * 精选推荐 大于10个时 分页展示
     *
     * @param paramMap
     * @return
     */
    @POST
    @Path("/loadRecommendGoodsByPage")
    public Response loadRecommendGoods(Map<String, Object> paramMap) {
        Long pageIndex = CommonUtils.getLong(paramMap, "pageIndex");
        int pageSize = 20;
        if (pageIndex == null || pageIndex.intValue() > 3 || pageIndex < 1) {
            return Response.fail(BusinessErrorCode.PARAM_VALUE_ERROR);
        }
        Map<String, Object> resultMap = new HashMap<>();
        int pageBegin = pageSize * (pageIndex.intValue() - 1);
        try {
            Pagination<GoodsBasicInfoEntity> recommendGoods = goodService.loadRecommendGoods(pageBegin,
                    pageSize);
            for (GoodsBasicInfoEntity goods : recommendGoods.getDataList()) {
                BigDecimal price = commonService.calculateGoodsPrice(goods.getGoodId(),
                        goods.getGoodsStockId());
                goods.setGoodsPrice(price);
                goods.setGoodsPriceFirst(new BigDecimal("0.1").multiply(price));// 设置首付价=商品价*10%
                Long marketPrice = goodsStockInfoRepository.getMaxMarketPriceByGoodsId(goods.getGoodId());
                goods.setMarketPrice(new BigDecimal(marketPrice));
                if ("jd".equals(goods.getSource())) {
                    goods.setGoodsLogoUrlNew("http://img13.360buyimg.com/n3/" + goods.getGoodsLogoUrl());
                    goods.setGoodsSiftUrlNew(imageService.getImageUrl(goods.getGoodsSiftUrl()));
                } else {
                    goods.setGoodsLogoUrlNew(imageService.getImageUrl(goods.getGoodsLogoUrl()));
                    goods.setGoodsSiftUrlNew(imageService.getImageUrl(goods.getGoodsSiftUrl()));
                    goods.setGoodsLogoUrl(EncodeUtils.base64Encode(goods.getGoodsLogoUrl()));
                    goods.setGoodsSiftUrl(EncodeUtils.base64Encode(goods.getGoodsSiftUrl()));
                }
            }
            resultMap.put("recommendGoods", recommendGoods);
            return Response.successResponse(resultMap);
        } catch (BusinessException e) {
            LOGGER.error("loadRecommendGoodsByPage fail", e);
            return Response.fail(BusinessErrorCode.LOAD_INFO_FAILED);
        }

    }

    @POST
    @Path("/allRecommentGoods")
    public Response allRecommentGoods(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Pagination<GoodsBasicInfoEntity> recommendGoods = goodService.loadRecommendGoods(1, 50);
            LOGGER.info("精选推荐查询结果:{}", GsonUtils.toJson(recommendGoods.getDataList()));
            if (CollectionUtils.isEmpty(recommendGoods.getDataList())) {
                return Response.fail("数据库有误！");
            }
            for (GoodsBasicInfoEntity goodsBaseInfoEntyty : recommendGoods.getDataList()) {
                BigDecimal price = commonService.calculateGoodsPrice(goodsBaseInfoEntyty.getGoodId(),
                        goodsBaseInfoEntyty.getGoodsStockId());
                goodsBaseInfoEntyty.setGoodsPrice(price);
                goodsBaseInfoEntyty.setGoodsPriceFirst(new BigDecimal("0.1").multiply(price));// 设置首付价=商品价*10%
                Long marketPrice = goodsStockInfoRepository.getMaxMarketPriceByGoodsId(goodsBaseInfoEntyty.getGoodId());
                goodsBaseInfoEntyty.setMarketPrice(new BigDecimal(marketPrice));
                if ("jd".equals(goodsBaseInfoEntyty.getSource())) {
                    goodsBaseInfoEntyty.setGoodsLogoUrlNew("http://img13.360buyimg.com/n3/" + goodsBaseInfoEntyty.getGoodsLogoUrl());
                    goodsBaseInfoEntyty.setGoodsSiftUrlNew(imageService.getImageUrl(goodsBaseInfoEntyty.getGoodsSiftUrl()));
                } else {
                    goodsBaseInfoEntyty.setGoodsLogoUrlNew(imageService.getImageUrl(goodsBaseInfoEntyty.getGoodsLogoUrl()));
                    goodsBaseInfoEntyty.setGoodsSiftUrlNew(imageService.getImageUrl(goodsBaseInfoEntyty.getGoodsSiftUrl()));
                    goodsBaseInfoEntyty.setGoodsLogoUrl(EncodeUtils.base64Encode(goodsBaseInfoEntyty.getGoodsLogoUrl()));
                    goodsBaseInfoEntyty.setGoodsSiftUrl(EncodeUtils.base64Encode(goodsBaseInfoEntyty.getGoodsSiftUrl()));
                }
            }
            resultMap.put("recommendGoods", recommendGoods);
            return Response.successResponse(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(BusinessErrorCode.LOAD_INFO_FAILED);
        }
    }




    /**
     *
     * @param goodsId
     * @return
     * @throws BusinessException
     */
    private BigDecimal getGoodsPrice(Long goodsId) throws BusinessException {
        // 根据goodsid查询库存，找出最低售价显示前端
        Map<String, Object> minPriceGoodsMap = goodsService.getMinPriceGoods(goodsId);
        BigDecimal goodsPrice = (BigDecimal) minPriceGoodsMap.get("minPrice");


//        List<GoodsStockInfoEntity> goodsStocks = goodsService.loadDetailInfoByGoodsId(goodsId);
//        if (goodsStocks == null || goodsStocks.size() == 0) {
//            LOGGER.error("数据异常，商品id为:{}无对应库存", goodsId.toString());
//            throw new BusinessException("数据异常");
//        }
//        BigDecimal goodsPrice = goodsStocks.get(0).getGoodsPrice();
//        for (GoodsStockInfoEntity goodsStockInfoEntity : goodsStocks) {
//            if (goodsPrice.compareTo(goodsStockInfoEntity.getGoodsPrice()) > 0) {
//                goodsPrice = goodsStockInfoEntity.getGoodsPrice();
//            }
//        }

        return goodsPrice;
    }

    /**
     * 其它分类页面
     *
     * @param paramMap
     * @return
     */
    @POST
    @Path("/otherCategoryGoods")
    public Response otherCategoryGoods(Map<String, Object> paramMap) {
        List<OtherCategoryGoodsVo> list = Lists.newArrayList();
        // 参数验证
        Long categoryId = CommonUtils.getLong(paramMap, "categoryId");
        if (categoryId == null) {
            return Response.fail(BusinessErrorCode.PARAM_IS_EMPTY.getMsg());
        }
        try {
            list = categoryInfoService.otherCategoryGoods(categoryId);
        } catch (BusinessException e) {
            LOGGER.error("根据一级类目查询所有二级类目下商品失败,一级类目id:{}", categoryId, e);
            return Response.fail(e.getErrorDesc());
        }

        return Response.successResponse(list);
    }
    public GoodsVo goodsToGoodVo(Goods goods) throws BusinessException {
        GoodsVo vo = new GoodsVo();
//        vo.setFirstPrice(goods.getFirstPrice());
        vo.setGoodId(goods.getGoodId());
        vo.setGoodsLogoUrl(goods.getGoodsLogoUrl());
        vo.setGoodsLogoUrlNew(goods.getGoodsLogoUrlNew());
        vo.setGoodsName(goods.getGoodsName());
//        vo.setGoodsPrice(goods.getGoodsPrice());
        vo.setGoodsStockId(goods.getGoodsStockId());
        vo.setGoodsTitle(goods.getGoodsTitle());
        vo.setId(goods.getId());
        vo.setSource(goods.getSource());
        Map<String,Object> result = goodService.getMinPriceGoods(goods.getGoodId());
        vo.setGoodsPrice((BigDecimal)result.get("minPrice"));
        vo.setFirstPrice(((BigDecimal) result.get("minPrice")).multiply(new BigDecimal("0.1")).setScale(2,
                BigDecimal.ROUND_DOWN));
        return vo;
    }
    
}
