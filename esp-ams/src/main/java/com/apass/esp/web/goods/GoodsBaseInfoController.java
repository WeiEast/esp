package com.apass.esp.web.goods;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.apass.esp.domain.Response;
import com.apass.esp.domain.dto.goods.BannerPicDto;
import com.apass.esp.domain.dto.goods.GoodsStockSkuDto;
import com.apass.esp.domain.dto.goods.LogoFileModel;
import com.apass.esp.domain.dto.goods.StockInfoFileModel;
import com.apass.esp.domain.entity.Category;
import com.apass.esp.domain.entity.CategoryDo;
import com.apass.esp.domain.entity.GoodsAttr;
import com.apass.esp.domain.entity.banner.BannerInfoEntity;
import com.apass.esp.domain.entity.common.SystemParamEntity;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsStockInfoEntity;
import com.apass.esp.domain.entity.merchant.MerchantInfoEntity;
import com.apass.esp.domain.entity.rbac.UsersDO;
import com.apass.esp.domain.enums.GoodStatus;
import com.apass.esp.domain.enums.GoodsType;
import com.apass.esp.domain.enums.SourceType;
import com.apass.esp.search.dao.GoodsEsDao;
import com.apass.esp.search.entity.Goods;
import com.apass.esp.search.manager.IndexManager;
import com.apass.esp.service.UsersService;
import com.apass.esp.service.banner.BannerInfoService;
import com.apass.esp.service.category.CategoryInfoService;
import com.apass.esp.service.common.SystemParamService;
import com.apass.esp.service.goods.GoodsAttrService;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.goods.GoodsStockInfoService;
import com.apass.esp.service.jd.JdGoodsInfoService;
import com.apass.esp.service.merchant.MerchantInforService;
import com.apass.esp.service.order.OrderService;
import com.apass.esp.utils.FileUtilsCommons;
import com.apass.esp.utils.ImageTools;
import com.apass.esp.utils.PaginationManage;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.log.LogAnnotion;
import com.apass.gfb.framework.log.LogValueTypeEnum;
import com.apass.gfb.framework.mybatis.page.Page;
import com.apass.gfb.framework.security.toolkit.SpringSecurityUtils;
import com.apass.gfb.framework.utils.BaseConstants.CommonCode;
import com.apass.gfb.framework.utils.GsonUtils;
import com.apass.gfb.framework.utils.HttpWebUtils;
import com.apass.gfb.framework.utils.ImageUtils;
import com.google.common.collect.Maps;


/**
 * 商品管理
 *
 * @author zwd
 * @version 1.0
 * @date 2016年12月21日
 */
@Controller
@RequestMapping("/application/goods/management")
public class GoodsBaseInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsBaseInfoController.class);
    private static final String SUCCESS = "SUCCESS";
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private BannerInfoService bannerInfoService;
    @Autowired
    private GoodsStockInfoService goodsStockInfoService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private SystemParamService systemParamService;

    @Autowired
    private MerchantInforService merchantInforService;

    @Autowired
    private CategoryInfoService categoryInfoService;

    @Autowired
    private JdGoodsInfoService jdGoodsInfoService;
    
    @Autowired
    private GoodsEsDao goodsEsDao;
    @Autowired
    private OrderService orderService;

    /**
     * 图片服务器地址
     */
    @Value("${nfs.rootPath}")
    private String rootPath;

    @Value("${nfs.banner}")
    private String nfsBanner;

    @Value("${nfs.goods}")
    private String nfsGoods;

    /**
     * form表单提交 Date类型数据绑定 <功能详细描述>
     *
     * @param binder
     * @see [类、类#方法、类#成员]
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    /**
     * 商品管理页面加载
     */
    @RequestMapping("/page")
    public ModelAndView handlePage() {
        UsersDO users = usersService.loadBasicInfo();
        Map<String, Object> map = Maps.newHashMap();
        String merchantCode = users.getMerchantCode();
        map.put("userMechantCode", merchantCode);
        try {
            // 获取商户状态
            MerchantInfoEntity merchantInfoEntity = merchantInforService.queryByMerchantCode(merchantCode);
            if (merchantInfoEntity != null) {
                String merchantStatus = merchantInfoEntity.getStatus();
                map.put("merchantStatus", merchantStatus);
            }
            // 系统参数费率
            map.put("goodsPriceRate", 1);
            map.put("priceCostRate", systemParamService.querySystemParamInfo().get(0).getPriceCostRate());
            map.put("merchantSettleRate", systemParamService.querySystemParamInfo().get(0)
                    .getMerchantSettleRate());

            if (SpringSecurityUtils.hasPermission("GOODS_INFO_EDIT")) {
                map.put("grantedAuthority", "permission");
            }
            if (SpringSecurityUtils.hasPermission("GOODS_COSTPRICE_IF")) {
                map.put("goodCostpriceIf", "permission");
            }
        } catch (BusinessException e) {
            LOGGER.error("查询系统参数异常", e);
        }
        LOGGER.info("商品初始化成功...");
        return new ModelAndView("goods/goodsInfo-list", map);
    }

    /**
     * 商品管理页面加载
     */
    @RequestMapping("/checkPage")
    public ModelAndView checkPage() {
        Map<String, Object> map = Maps.newHashMap();
        try {
            // 系统参数费率
            map.put("goodsPriceRate", 1);
            map.put("priceCostRate", systemParamService.querySystemParamInfo().get(0).getPriceCostRate());
            map.put("merchantSettleRate", systemParamService.querySystemParamInfo().get(0)
                    .getMerchantSettleRate());

            if (SpringSecurityUtils.hasPermission("GOODS_CHECK_BATCH")) {
                map.put("grantedAuthority", "permission");
            }
        } catch (BusinessException e) {
            LOGGER.error("查询系统参数异常", e);
        }
        LOGGER.info("商品审核初始化成功...");
        return new ModelAndView("goods/goodsInfoCheck-list", map);
    }

    /**
     * 商品管理页面加载
     */
    @RequestMapping("/bBenPage")
    public ModelAndView bBenPage() {
        Map<String, Object> map = Maps.newHashMap();
        if (SpringSecurityUtils.hasPermission("GOODS_BBEN_CHECK_BATCH")) {
            map.put("grantedAuthority", "permission");
        }
        return new ModelAndView("goods/goodsInfoBben-list", map);
    }

    /**
     * 商品类目列表
     */
    @ResponseBody
    @RequestMapping("/categoryList")
    public Response categoryList() {
        try {
            List<CategoryDo> categoryList = categoryInfoService.goodsCategoryList();
            return Response.success("success", categoryList);
        } catch (Exception e) {
            LOGGER.error("商品类目列表加载失败！", e);
            return Response.fail("商品类目列表加载失败！");
        }
    }

    /**
     * 商品类目列表:只查前两级类目
     */
    @ResponseBody
    @RequestMapping("/categoryList2")
    public Response categoryList2() {
        try {
            List<CategoryDo> categoryList = categoryInfoService.goodsCategoryList2();
            return Response.success("success", categoryList);
        } catch (Exception e) {
            LOGGER.error("商品类目列表加载失败！", e);
            return Response.fail("商品类目列表加载失败！");
        }
    }

    /**
     * 商品管理分页json
     */
    @ResponseBody
    @RequestMapping("/pagelist")
    public ResponsePageBody<GoodsInfoEntity> handlePageList(HttpServletRequest request) {
        ResponsePageBody<GoodsInfoEntity> respBody = new ResponsePageBody<GoodsInfoEntity>();
        try {
            // if (null == usersService.loadBasicInfo().getMerchantCode()) {
            // respBody.setTotal(0);
            // respBody.setStatus("1");
            // return respBody;
            // }
            String pageNo = HttpWebUtils.getValue(request, "page");
            String pageSize = HttpWebUtils.getValue(request, "rows");
            String goodsName = HttpWebUtils.getValue(request, "goodsName");
            String goodsType = HttpWebUtils.getValue(request, "goodsType");
            String merchantName = HttpWebUtils.getValue(request, "merchantName");
            String merchantType = HttpWebUtils.getValue(request, "merchantType");
            String goodsCategoryCombo = HttpWebUtils.getValue(request, "goodsCategoryCombo");
            String status = HttpWebUtils.getValue(request, "goodsStatus");

            // String isAll = HttpWebUtils.getValue(request, "isAll");// 是否查询所有
            String categoryId1 = HttpWebUtils.getValue(request, "categoryId1");
            String categoryId2 = HttpWebUtils.getValue(request, "categoryId2");
            String categoryId3 = HttpWebUtils.getValue(request, "categoryId3");
            String goodsCode = HttpWebUtils.getValue(request, "goodsCode");

            GoodsInfoEntity goodsInfoEntity = new GoodsInfoEntity();
            goodsInfoEntity.setGoodsCode(goodsCode);
            goodsInfoEntity.setGoodsName(goodsName);
            goodsInfoEntity.setStatus(status);
            if(StringUtils.isNotBlank(status) && status.contains("G04")){
                String[] statuArr = status.split(",");
                List<String> statuList = Arrays.asList(statuArr);
                goodsInfoEntity.setStatuList(statuList);
                goodsInfoEntity.setStatus(null);
            }
            goodsInfoEntity.setGoodsType(goodsType);
            goodsInfoEntity.setMerchantName(merchantName);
            goodsInfoEntity.setMerchantType(merchantType);
            if (StringUtils.isNotBlank(goodsCategoryCombo)) {
                String[] aArray = goodsCategoryCombo.split("_");
                String level = aArray[0];
                String id = aArray[1];
                if ("1".equals(level)) {
                    if (!("-1".equals(id))) {
                        goodsInfoEntity.setCategoryId1(Long.valueOf(id));
                    }
                } else if ("2".equals(level)) {
                    goodsInfoEntity.setCategoryId2(Long.valueOf(id));
                } else if ("3".equals(level)) {
                    goodsInfoEntity.setCategoryId3(Long.valueOf(id));
                }
            }

            if (!StringUtils.isAnyBlank(categoryId1, categoryId2, categoryId3)) {
                goodsInfoEntity.setCategoryId1(Long.valueOf(categoryId1));
                goodsInfoEntity.setCategoryId2(Long.valueOf(categoryId2));
                goodsInfoEntity.setCategoryId3(Long.valueOf(categoryId3));
            }

            goodsInfoEntity.setMerchantCode(usersService.loadBasicInfo().getMerchantCode());

            PaginationManage<GoodsInfoEntity> pagination = goodsService.pageList(goodsInfoEntity, pageNo,pageSize);

            if (pagination == null) {
                respBody.setTotal(0);
                respBody.setStatus("1");
                return respBody;
            }
            for (int i = 0; i < pagination.getDataList().size(); i++) {
                pagination
                        .getDataList()
                        .get(i)
                        .setColFalgt(
                                goodsService.ifRate(Long.valueOf(pagination.getDataList().get(i).getId()),
                                        systemParamService.querySystemParamInfo().get(0)
                                                .getMerchantSettleRate()));

                Long categoryId = pagination.getDataList().get(i).getCategoryId3();
                Category category = categoryInfoService.selectNameById(categoryId);
                if (null != category) {
                    pagination.getDataList().get(i).setCategoryName3(category.getCategoryName());
                }
            }
            respBody.setTotal(pagination.getTotalCount());
            respBody.setRows(pagination.getDataList());
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            LOGGER.error("商品列表查询失败", e);
            respBody.setMsg("商品列表查询失败");
        }
        return respBody;
    }

    /**
     * 商品基本信息录入
     *
     * @param pageModel
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @LogAnnotion(operationType = "商品添加", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response add(@ModelAttribute("pageModel") GoodsInfoEntity pageModel) {
        String message = SUCCESS;
        GoodsInfoEntity goodsInfo = null;

        if (StringUtils.isAnyBlank(pageModel.getMerchantCode(), 
                pageModel.getGoodsModel(),pageModel.getGoodsName(), 
                pageModel.getGoodsTitle())
                || pageModel.getListTime().equals("")
                || pageModel.getCategoryId1().equals("")
                || pageModel.getCategoryId2().equals("")
                || pageModel.getCategoryId3().equals("")
                || pageModel.getSupport7dRefund().equals("")) {
            message = "参数有误,请确认再提交！";
            return Response.fail(message);
        }

        try {
            pageModel.setStatus(GoodStatus.GOOD_NEW.getCode());
            pageModel.setIsDelete("01");
            pageModel.setGoodsType(GoodsType.GOOD_NORMAL.getCode());
            pageModel.setCreateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());// 创建人
            pageModel.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());// 更新人
            pageModel.setNewCreatDate(new Date());
            pageModel.setSource("");
            pageModel.setExternalId("");
            pageModel.setGoodsSkuType("");
            Integer sordNo = pageModel.getSordNo();
            if(sordNo != null){//如果有排序字段，判断同一二级类目下是否有相同排序商品。如果后，其后的都—sordNo都+1
                List<GoodsInfoEntity> goodsInfoEntities = goodsService.selectByCategoryId2(pageModel.getCategoryId2());
                for (GoodsInfoEntity goodsInfoEntity:goodsInfoEntities) {
                    if(sordNo == goodsInfoEntity.getSordNo()){
                        Map<String,Object> params = Maps.newHashMap();
                        params.put("categoryId2",pageModel.getCategoryId2());
                        params.put("sordNo",sordNo);
                        params.put("status",GoodStatus.GOOD_UP.getCode());
                        List<GoodsInfoEntity> goods= goodsService.selectByCategoryId2AndsordNo(params);
                        for (GoodsInfoEntity good: goods) {
                            good.setSordNo(good.getSordNo()+1);
                            good.setUpdateDate(new Date());
                            goodsService.updateService(good);
                        }
                    }
                }
            }
            goodsInfo = goodsService.insert(pageModel);
        }catch (BusinessException e) {
            LOGGER.error(e.getErrorDesc(), e);
            return Response.fail(e.getErrorDesc());
        } catch (Exception e) {
            LOGGER.error("商品添加失败!", e);
            return Response.fail("商品添加失败!");
        }
        return Response.success(message, goodsInfo);
    }
    /**
     * 商品基本信息修改
     *
     * @param pageModelEdit
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @LogAnnotion(operationType = "商品修改", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response edit(@ModelAttribute("pageModelEdit") GoodsInfoEntity pageModelEdit) {
        LOGGER.info("编辑商品，参数:{}",GsonUtils.toJson(pageModelEdit));
        String message = SUCCESS;
        if (StringUtils.isAnyBlank(pageModelEdit.getGoodsName(), pageModelEdit.getGoodsTitle())
                || pageModelEdit.getListTime().equals("")) {
            message = "参数有误,请确认再提交！";
            return Response.fail(message);
        }
        try {
//            String goodsName = URLDecoder.decode(pageModelEdit.getGoodsName(), "UTF-8");
            pageModelEdit.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());// 更新人
            goodsService.updateServiceForBaseInfoColler(pageModelEdit);
        } catch (Exception e) {
            LOGGER.error("编辑商品失败", e);
            return Response.fail("编辑商品失败");
        }
        return Response.success(message);
    }

    @ResponseBody
    @RequestMapping(value = "/editCategory", method = RequestMethod.POST)
    @LogAnnotion(operationType = "商品类目修改", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response editCategory(@ModelAttribute("pageModelEdit") GoodsInfoEntity pageModelEdit) {
        try {
        	pageModelEdit.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());// 更新人
            return goodsAttrService.editCategory(pageModelEdit);
        } catch (Exception e) {
            LOGGER.error("编辑商品失败", e);
            return Response.fail("编辑商品类目失败");
        }
    }

    @ResponseBody
    @RequestMapping("/loalEditor")
    public String loalEditor(HttpServletRequest request) {
        String id = HttpWebUtils.getValue(request, "id");
        GoodsInfoEntity result = goodsService.selectByGoodsId(Long.valueOf(id));
        if (null == result) {
            return "";
        }
        return result.getGoogsDetail();
    }

    /**
     * editor商品描述html
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/upeditor")
    public String upeditor(HttpServletRequest request) {
        String id = HttpWebUtils.getValue(request, "id");
        String goodsDetail = HttpWebUtils.getValue(request, "goodsDetail");
        String goodsContent = HttpWebUtils.getValue(request, "goodsContent");

        GoodsInfoEntity dto = new GoodsInfoEntity();
        dto.setId(Long.valueOf(id));
        dto.setGoogsDetail(goodsDetail);
        goodsService.updateServiceEdit(dto, goodsContent);
        return SUCCESS;
    }

    /**
     * banner图
     */
    @ResponseBody
    @RequestMapping("/goodsbannerList")
    public ResponsePageBody<BannerInfoEntity> goodsbannerList(HttpServletRequest request) {
        ResponsePageBody<BannerInfoEntity> pagebody = new ResponsePageBody<BannerInfoEntity>();
        try {
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            if (StringUtils.isBlank(goodsId)) {
                return null;
            }
            List<BannerInfoEntity> list = bannerInfoService.loadIndexBanners(goodsId);
            pagebody.setRows(list);
            pagebody.setTotal(list.size());
            pagebody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            LOGGER.error("banner图列表查询失败", e);
        }
        return pagebody;
    }


    /**
     * 上传banner图
     *
     * @param bannerDto
     */
    @ResponseBody
    @RequestMapping(value = "/addBanner", method = RequestMethod.POST)
    @LogAnnotion(operationType = "商品大图", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response addBanner(@ModelAttribute("bannerModel") BannerPicDto bannerDto) {

        List<BannerInfoEntity> listEntity = bannerInfoService.loadIndexBanners(bannerDto.getBannerGoodsId());
        for (BannerInfoEntity bannerInfoEntity : listEntity) {
            if (bannerInfoEntity.getBannerOrder() == Long.valueOf(bannerDto.getBannerPicOrder())) {
                return Response.fail("排序已存在请重新输入排序！");
            }
        }
        if (listEntity.size() >= 5) {
            return Response.fail("最多只能上传5张商品大图,请删除后再上传！");
        }

        try {
            MultipartFile file = bannerDto.getBannerPicFile();
            String imgType = ImageTools.getImgType(file);
            String fileName = "banner_" + bannerDto.getBannerGoodsId() + "_" + System.currentTimeMillis()
                    + "_" + bannerDto.getBannerPicOrder() + "." + imgType;
            String url = nfsBanner + bannerDto.getBannerGoodsId() + "/" + fileName;

            /**
             * 图片校验
             */
            boolean checkGoodBannerImgSize = ImageTools.checkGoodBannerImgSize(file);// 尺寸
            boolean checkImgType = ImageTools.checkImgType(file);// 类型
            int size = file.getInputStream().available();

            if (!(checkGoodBannerImgSize && checkImgType)) {
                file.getInputStream().close();// 750*750px;大小：≤300kb;.jpg .png
                return Response.fail("文件尺寸不符,上传图片尺寸必须是宽：750px,高：750px,格式：.jpg,.png", url);
            } else if (size > 1024 * 300) {
                file.getInputStream().close();
                return Response.fail("文件不能大于300kb!", url);
            }

            /**
             * 上传文件
             */
            FileUtilsCommons.uploadFilesUtil(rootPath, url, bannerDto.getBannerPicFile());
            /**
             * 保存信息至banner表
             */
            BannerInfoEntity entity = new BannerInfoEntity();
            entity.setBannerCategory("1");// 未填入
            entity.setBannerImgUrl(url);
            entity.setBannerOrder(Long.valueOf(bannerDto.getBannerPicOrder()));
            entity.setBannerName(fileName);
            entity.setBannerType(bannerDto.getBannerGoodsId());
            entity.setAttr("");
            entity.setAttrVal("");
            entity.setCreateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
            entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
            bannerInfoService.insert(entity);
            /**
             * 同时要修改goods_base中的update_user 和 update_time
             */
            updateDB(bannerDto.getBannerGoodsId());

            return Response.success("success");

        } catch (Exception e) {
            LOGGER.error("上传商品大图失败!", e);
            return Response.fail("上传商品大图失败!");
        }
    }


    /**
     * 更新数据库的更新人和更新时间
     */
    public void updateDB(String goodsId) {
        GoodsInfoEntity entity = new GoodsInfoEntity();
        entity.setId(Long.valueOf(goodsId));
        entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
        entity.setUpdateDate(new Date());
        goodsService.updateService(entity);
    }

    /**
     * 删除banner图
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/delBanner")
    public String delBanner(HttpServletRequest request) {
        String id = HttpWebUtils.getValue(request, "id");
        bannerInfoService.delete(Long.valueOf(id));
        return SUCCESS;
    }

    /**
     * 上架
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/shelves")
    @LogAnnotion(operationType = "商品上架", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public String shelves(HttpServletRequest request) {
        String id = HttpWebUtils.getValue(request, "id");
        String source = HttpWebUtils.getValue(request, "source");
        String listTime = HttpWebUtils.getValue(request, "listTime");
        String delistTime = HttpWebUtils.getValue(request, "delistTime");
        GoodsInfoEntity goodsEntity = goodsService.selectByGoodsId(Long.valueOf(id));
        if (null == goodsEntity) {
            return "商品不存在！";
        }

        GoodsStockInfoEntity goodsStockInfoEntity = new GoodsStockInfoEntity();
        goodsStockInfoEntity.setGoodsId(Long.valueOf(id));
        PaginationManage<GoodsStockInfoEntity> list = null;
        try {
            list = goodsStockInfoService.pageList(goodsStockInfoEntity,
                "0", "10");
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        List<GoodsStockInfoEntity> stockList = list.getDataList();
        if (stockList.isEmpty()) {
            return "商品库存为空,请添加！";
        }
        GoodsInfoEntity entity = new GoodsInfoEntity();
        if (!StringUtils.equals(source,SourceType.JD.getCode()) && !StringUtils.equals(source,SourceType.WZ.getCode())) {
            List<BannerInfoEntity> bannerList = bannerInfoService.loadIndexBanners(id);// banner图
            if (bannerList.isEmpty()) {
                return "商品大图为空，请上传！";
            }
            if (StringUtils.isBlank(goodsEntity.getGoodsLogoUrl())) {
                return "商品墙图片为空，请上传！";
            }
            if (StringUtils.isBlank(goodsEntity.getGoogsDetail())) {
                return "商品详情不能为空,请添加！";
            }
            if (goodsEntity.getCategoryId1() == null || goodsEntity.getCategoryId2() == null
                    || goodsEntity.getCategoryId3() == null) {
                return "商品类目不能为空，请先选择类目！";
            }
        } else {
            if (StringUtils.isAnyBlank(listTime)) {
                return "商品上架时间不能为空，请先选择类目！";
            }

            //查询商品价格
            List<GoodsStockSkuDto> goodsStockSkuInfo = goodsStockInfoService.getGoodsStockSkuInfo(Long.valueOf(id));
            if(CollectionUtils.isEmpty(goodsStockSkuInfo) || goodsStockSkuInfo.size()>2){
                LOGGER.info("京东商品库存有误,商品id:{}",id);
                return "微知商品库存有误";
            }

            if(goodsStockSkuInfo.get(0).getGoodsCostPrice().compareTo(new BigDecimal(99))<0){
                return "微知协议价格低于99元，不能上架";
            }
            //验证商品是否可售（当验证为不可售时，提示操作人员）
            if(!orderService.checkGoodsSalesOrNot(goodsEntity.getExternalId())){
            	 return "该微知商品暂时不可售，不能上架";
            }
        }
        SystemParamEntity systemParamEntity = null;
        try {
            systemParamEntity = systemParamService.querySystemParamInfo().get(0);
        } catch (Exception e) {
            return "查询系统参数错误";
        }
        for (GoodsStockInfoEntity goodsStockInfoEntity1 : stockList) {
            BigDecimal goodsPrice = goodsStockInfoEntity1.getGoodsPrice();
            BigDecimal goodsCostPrice = goodsStockInfoEntity1.getGoodsCostPrice();
            BigDecimal dividePoint = goodsPrice.divide(goodsCostPrice, 4, BigDecimal.ROUND_DOWN);
            BigDecimal dividePoint1 = systemParamEntity.getPriceCostRate().multiply(new BigDecimal(0.01))
                    .setScale(4, BigDecimal.ROUND_DOWN);
            if (StringUtils.equals(source,SourceType.JD.getCode()) || StringUtils.equals(source,SourceType.WZ.getCode())) {
                String skuId = goodsEntity.getExternalId();
                Map<String, Object> descMap = new HashMap<String, Object>();
                try {
                    descMap = jdGoodsInfoService.getJdGoodsSimilarSku(Long.valueOf(skuId));

                } catch (Exception e) {
                    return "京东接口报错";
                }
                String jdGoodsSimilarSku = (String) descMap.get("jdGoodsSimilarSku");
                int jdSimilarSkuListSize = (int) descMap.get("jdSimilarSkuListSize");
                if (StringUtils.isBlank(jdGoodsSimilarSku) && jdSimilarSkuListSize > 0) {
                    return "该京东商品无法匹配规格无法上架！";
                }
                entity.setAttrDesc(jdGoodsSimilarSku);
            }
            // 商品售价除以成本价小于保本率
            if (dividePoint.compareTo(dividePoint1) == -1) {
                entity.setId(Long.valueOf(id));
                entity.setStatus(GoodStatus.GOOD_BBEN.getCode());
                entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
                goodsService.updateService(entity);
                return "该商品已进入保本率审核页面";
            } else {
                entity.setId(Long.valueOf(id));
                entity.setStatus(GoodStatus.GOOD_NOCHECK.getCode());
                entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
                goodsService.updateService(entity);
                return SUCCESS;
            }
        }
        return SUCCESS;
    }

    /**
     * 下架
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/shelf")
    @LogAnnotion(operationType = "商品下架", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public String shelf(HttpServletRequest request) {
        String id = HttpWebUtils.getValue(request, "id");
        // String source = HttpWebUtils.getValue(request, "source");
        GoodsInfoEntity entity = new GoodsInfoEntity();
        entity.setId(Long.valueOf(id));
        entity.setStatus(GoodStatus.GOOD_DOWN.getCode());
        entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
        Integer count = goodsService.updateService(entity);
        if(count == 1){
            GoodsInfoEntity entity2 = goodsService.selectByGoodsId(entity.getId());
            Goods goods = new Goods();
            goods.setId(entity2.getGoodId().intValue());
            LOGGER.info("商品下架，删除索引传递的参数:{}",GsonUtils.toJson(goods));
            goodsEsDao.delete(goods);
        }
        return SUCCESS;
    }

    /**
     * check复核
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkview")
    @LogAnnotion(operationType = "商品复核", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public String checkview(HttpServletRequest request) {
        String ids = HttpWebUtils.getValue(request, "ids");
        String flag = HttpWebUtils.getValue(request, "flag");
        String message = HttpWebUtils.getValue(request, "message");

        if (!StringUtils.isBlank(ids)) {
            ids = ids.substring(1, ids.length() - 1);
            String[] strArr = ids.split(",");
            if (null != strArr && strArr.length >= 0) {
                for (int i = 0; i < strArr.length; i++) {
                    GoodsInfoEntity entity = new GoodsInfoEntity();
                    entity.setId(Long.valueOf(strArr[i]));
                    if ("reject".equals(flag)) {
                        entity.setStatus(GoodStatus.GOOD_NEW.getCode());
                    } else {
                        entity.setStatus(GoodStatus.GOOD_UP.getCode());
                    }
                    entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
                    entity.setRemark(message);
                    Integer count = goodsService.updateService(entity);
                    if(count == 1 && !"reject".equals(flag)){
                        GoodsInfoEntity entity2 = goodsService.selectByGoodsId(entity.getId());
                        Goods goods = goodsService.goodsInfoToGoods(entity2);
                        LOGGER.info("审核通过,添加索引传递的参数:{}",GsonUtils.toJson(goods));
                        //TODO在ES中相似规格的商品只上架一件（即：如果商品多规格则在ES中添加一个规格）
                        String source=entity2.getSource();
                        String skuId="";
                        Boolean  goodsInESNumFalg=true;
                        if(null !=source && SourceType.WZ.getCode().equals(source)){
                        	skuId=entity2.getExternalId();
                        	TreeSet<String> similarSkuIds=jdGoodsInfoService.getJdSimilarSkuIdList(skuId);
                        	for (String string : similarSkuIds) {
                        		GoodsInfoEntity goodsInfo=goodsService.selectGoodsByExternalId(string);
                        		Goods goodsfromES=IndexManager.goodSearchFromESBySkuId(goodsInfo.getId());
                        		if(null !=goodsfromES){//该商品的相似规格已经在ES中存在
                        			goodsInESNumFalg=false;
                        			break;
                        		}
                        	}
                        }
                        //如果ES中没有商品规格中任何一个，则添加到ES中
                        if(goodsInESNumFalg){
                        	  goodsEsDao.add(goods);
                        }
                    }
                }
            }
        }
        return SUCCESS;
    }

    @ResponseBody
    @RequestMapping("/bBencheckview")
    @LogAnnotion(operationType = "商品信息保本复核", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public String bBencheckview(HttpServletRequest request) {
        String ids = HttpWebUtils.getValue(request, "ids");
        String flag = HttpWebUtils.getValue(request, "flag");
        if (!StringUtils.isBlank(ids)) {
            ids = ids.substring(1, ids.length() - 1);
            String[] strArr = ids.split(",");
            if (null != strArr && strArr.length >= 0) {
                for (int i = 0; i < strArr.length; i++) {
                    GoodsInfoEntity entity = new GoodsInfoEntity();
                    entity.setId(Long.valueOf(strArr[i]));
                    if (!"reject".equals(flag)) {
                        entity.setStatus(GoodStatus.GOOD_NOCHECK.getCode());
                    } else {
                        entity.setStatus(GoodStatus.GOOD_NEW.getCode());
                    }
                    entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
                    goodsService.updateService(entity);
                }
            }
        }
        return SUCCESS;
    }

    /**
     * loadLogo
     *
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/loadPic")
    public String loadPic(HttpServletRequest request) throws Exception {
        return FileUtilsCommons.loadPicBase64String(rootPath, HttpWebUtils.getValue(request, "picUrl"));

    }

    /**
     * 上传logo
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/uplogoFile", method = RequestMethod.POST)
    @LogAnnotion(operationType = "商品墙图片", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response uplogoFile(@ModelAttribute("logoFileModel") LogoFileModel logoFileModel) {
        try {
            Integer random = (int) (Math.random() * 9000 + 1000);// 生成4位随机数
            MultipartFile file = logoFileModel.getEditGoodsLogoFile();
            String imgType = ImageTools.getImgType(file);
            String fileName = "logo_" + random + logoFileModel.getEditLogogoodsId() + "." + imgType;
            String url = nfsGoods + logoFileModel.getEditLogogoodsId() + "/" + fileName;

            // 图片验证
            boolean checkLogoImgSize = ImageTools.checkLogoImgSize(file);// 尺寸
            boolean checkImgType = ImageTools.checkImgType(file);// 类型
            int size = file.getInputStream().available();

            if (!(checkLogoImgSize && checkImgType)) {
                file.getInputStream().close();// 367*268px;.jpg .png
                return Response.fail("文件尺寸不符,上传图片尺寸必须是宽：350px,高：350px,格式：.jpg,.png", url);
            } else if (size > 1024 * 300) {
                file.getInputStream().close();
                return Response.fail("文件不能大于300kb!", url);
            }

            /**
             * 上传文件
             */
            FileUtilsCommons.uploadFilesUtil(rootPath, url, logoFileModel.getEditGoodsLogoFile());
            /**
             * s 保存信息至banner表
             */
            GoodsInfoEntity entity = new GoodsInfoEntity();
            entity.setId(Long.valueOf(logoFileModel.getEditLogogoodsId()));
            entity.setGoodsLogoUrl(url);
            entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
            goodsService.updateService(entity);
            return Response.success("上传成功", url);

        } catch (Exception e) {
            LOGGER.error("上传logo失败!", e);
            return Response.fail("上传logo失败!");
        }
    }
    /**
     * 商品预览
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/loadAllBannerPic")
    public ModelAndView loadAllBannerPic(HttpServletRequest request) throws Exception {
        Map<String, Object> map = Maps.newHashMap();
        String id = HttpWebUtils.getValue(request, "id");
        String view = HttpWebUtils.getValue(request, "view");
        List<BannerInfoEntity> bannerList = bannerInfoService.loadIndexBanners(id);// banner图
        if (!bannerList.isEmpty()) {
            for (int i = 0; i < bannerList.size(); i++) {
                String colorBigSquare = ImageUtils.imageToBase64(rootPath
                        + bannerList.get(i).getBannerImgUrl());
                if (colorBigSquare.length() > 0) {
                    map.put("goodsBanner_" + (i + 1), colorBigSquare);
                }
            }
        }
        GoodsInfoEntity goodsInfo = goodsService.selectByGoodsId(Long.valueOf(id));
        List<GoodsStockInfoEntity> goodsStockList = goodsService.loadDetailInfoByGoodsId(Long.valueOf(id));
        map.put("previewGoodsName", goodsInfo.getGoodsName());// 商品名称
        map.put("goodsTitle", goodsInfo.getGoodsTitle());// 商品小标题
        int goodsStockNumber = goodsStockList.size();
        map.put("goodsStockNumber", goodsStockNumber);// 商品最小单元数目
        map.put("goodsSkuType", goodsInfo.getGoodsSkuType());// 商品最小单元分类
        BigDecimal min = null, max = null;// 商品价格区间
        if (goodsStockList != null && goodsStockNumber != 0) {
            min = max = goodsStockList.get(0).getGoodsPrice();
        }
        for (int i = 1; i < goodsStockList.size(); i++) {
            BigDecimal goodsPrice = goodsStockList.get(i).getGoodsPrice();
            if (min.compareTo(goodsPrice) == 1) {
                min = goodsPrice;
            }
            if (max.compareTo(goodsPrice) == -1) {
                max = goodsPrice;
            }
        }

        // 设值库存logo
        for (int i = 0; i < goodsStockList.size(); i++) {
            GoodsStockInfoEntity goodsStockInfoEntity = goodsStockList.get(i);
            goodsStockInfoEntity.setStockLogo(ImageUtils.imageToBase64(rootPath
                    + goodsStockInfoEntity.getStockLogo()));
        }
        if (null != min) { // 最小值
            map.put("goodsMinPrice", new DecimalFormat("0.00").format(min));// 价格区间
        } else {
            map.put("goodsMinPrice", "0.00");
        }

        if (null != max) {// 最大值
            map.put("goodsMaxPrice", new DecimalFormat("0.00").format(max));// 价格区间
        } else {
            map.put("goodsMaxPrice", "0.00");
        }
        map.put("view", view);
        map.put("goodsLogoUrl", ImageUtils.imageToBase64(rootPath + goodsInfo.getGoodsLogoUrl()));// 商品缩略图
        map.put("goodsStockList", goodsStockList);// 库存信息
        map.put("googsDetail", goodsInfo.getGoogsDetail());// 商品详情

        return new ModelAndView("goods/goodsPreviewProduct-view", map);
    }
    
    /**
     * 商品预览（非京东改为多规格）
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/loadAllBannerPicNotJd")
    public ModelAndView loadAllBannerPicNotJd(HttpServletRequest request) throws Exception {
        Map<String, Object> returnMap = new HashMap<>();
        String id = HttpWebUtils.getValue(request, "id");
        String view = HttpWebUtils.getValue(request, "view");
        String skuId=HttpWebUtils.getValue(request, "skuId");
        if(StringUtils.isNotBlank(skuId)){
            returnMap =goodsService.loadAllBannerPicNotJd2(skuId);
            returnMap.put("view", view);
            String image=goodsService.getDefaultImage(returnMap);
            returnMap.put("defaultImage", image);
            System.out.println(GsonUtils.toJson(returnMap));
            return new ModelAndView("goods/goodsPreviewProductNotJD-view", returnMap);
        }
        returnMap =goodsService.loadAllBannerPicNotJd(Long.parseLong(id));
        returnMap.put("view", view);
        String image=goodsService.getDefaultImage(returnMap);
        returnMap.put("defaultImage", image);
        System.out.println(GsonUtils.toJson(returnMap));
        return new ModelAndView("goods/goodsPreviewProductNotJD-view", returnMap);
    }
    
    /**
     * 商品预览--微知
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/loadAllBannerPicJD")
    public ModelAndView loadAllBannerPicJD(HttpServletRequest request) throws Exception {
        Map<String, Object> map = Maps.newHashMap();
        String id = HttpWebUtils.getValue(request, "id");
        String view = HttpWebUtils.getValue(request, "view");
        String skuId = HttpWebUtils.getValue(request, "skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            map = jdGoodsInfoService.getJdGoodsAllInfoBySku(Long.valueOf(skuId));
            map.put("view", view);
           System.out.println(GsonUtils.toJson(map)); 
            return new ModelAndView("goods/goodsPreviewProductJD-view", map);
        }
        GoodsInfoEntity goodsInfo = goodsService.selectByGoodsId(Long.valueOf(id));
        String externalId = goodsInfo.getExternalId();// 外部商品id
        map = jdGoodsInfoService.getJdGoodsAllInfoBySku(Long.valueOf(externalId).longValue());
        map.put("view", view);
        return new ModelAndView("goods/goodsPreviewProductJD-view", map);
    }

    @RequestMapping("/getsupport7dRefund")
    @ResponseBody
    public Response getsupport7dRefund(String skuId){
        String to7Return = goodsService.getsupport7dRefund(Long.valueOf(skuId));
        return Response.success(to7Return);
    }
    @Autowired
    private GoodsAttrService goodsAttrService;
    /**
     * 根据类目查询商品属性下拉框数据填充
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/goodAttrListByCategory", method = RequestMethod.GET)
    public List<GoodsAttr> goodAttrListByCategory(HttpServletRequest request) {
        try{
            String categoryId1 = HttpWebUtils.getValue(request, "categoryId1");
            return goodsAttrService.goodAttrListByCategory(categoryId1);
        }catch (Exception e) {
            LOGGER.error("商品属性下拉框载入失败!", e);
            return null;
        }
    }
    /**
     * 根据属性规格组合 排列 组合 列表
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/tableattrlist", method = RequestMethod.POST)
    public ResponsePageBody<StockInfoFileModel> tableattrlist(HttpServletRequest request) {
        ResponsePageBody<StockInfoFileModel> respBody = new ResponsePageBody<StockInfoFileModel>();
        try {
            // 获取分页数据
            Page page = new Page();
            String pageNo = HttpWebUtils.getValue(request, "page");
            String pageSize = HttpWebUtils.getValue(request, "rows");
            if(!StringUtils.isAnyBlank(pageNo,pageSize)){
                Integer pageNoNum = Integer.parseInt(pageNo);
                Integer pageSizeNum = Integer.parseInt(pageSize);
                page.setPage(pageNoNum <= 0 ? 1 : pageNoNum);
                page.setLimit(pageSizeNum <= 0 ? 1 : pageSizeNum);
            }
            // 获取页面查询条件
            String categoryname1 = HttpWebUtils.getValue(request, "categoryname1");
            String categoryname2 = HttpWebUtils.getValue(request, "categoryname2");
            String categoryname3 = HttpWebUtils.getValue(request, "categoryname3");
            // 获取分页结果返回给页面
            PaginationManage<StockInfoFileModel> pagination = goodsAttrService.tableattrlist(categoryname1,categoryname2,categoryname3,page);
            if (pagination == null||pagination.getDataList()==null||pagination.getDataList().size()==0) {
                respBody.setTotal(1);
                List<StockInfoFileModel> arr = new ArrayList<StockInfoFileModel>();
                StockInfoFileModel en = new StockInfoFileModel();
                en.setAttrnameByAfter(" ");
                arr.add(en);
                respBody.setRows(arr);
            }else{
                respBody.setTotal(pagination.getTotalCount());
                respBody.setRows(pagination.getDataList());
            }
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            respBody.setMsg("根据属性规格组合 排列 组合 列表 失败");
        }
        return respBody;
    }
    /**
     * 新增商品 保存库存 批量保存  商品的属性规格 和库存信息
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveGoodsCateAttrAndStock", method = RequestMethod.POST)
    @LogAnnotion(operationType = "新增商品  批量保存  商品的属性规格 和库存信息", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public Response saveGoodsCateAttrAndStock(HttpServletRequest request) {
        try{
            String user = SpringSecurityUtils.getCurrentUser();
            String[] goodsStock = request.getParameterValues("goodsStock");//HttpWebUtils.getValue(request, "goodsStock");
            String[] categorynameArr1 = request.getParameterValues("categorynameArr1");//HttpWebUtils.getValue(request, "categorynameArr1");
            String[] categorynameArr2 = request.getParameterValues("categorynameArr2");//HttpWebUtils.getValue(request, "categorynameArr2");
            String[] categorynameArr3 = request.getParameterValues("categorynameArr3");//HttpWebUtils.getValue(request, "categorynameArr3");
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            String status = HttpWebUtils.getValue(request, "status");
            List<StockInfoFileModel> listadd = JSONObject.parseObject(goodsStock[0], new TypeReference<List<StockInfoFileModel>>(){});
            List<GoodsStockInfoEntity> listenedit = JSONObject.parseObject(goodsStock[0], new TypeReference<List<GoodsStockInfoEntity>>(){});
            
            String[] stockLogo = request.getParameterValues("stockLogo");//HttpWebUtils.getValue(request, "goodsStock");
            List<StockInfoFileModel> stockLogoUrlAdd = JSONObject.parseObject(stockLogo[0], new TypeReference<List<StockInfoFileModel>>(){});
            List<GoodsStockInfoEntity> stockLogoUrledit = JSONObject.parseObject(stockLogo[0], new TypeReference<List<GoodsStockInfoEntity>>(){});
            return goodsAttrService.saveGoodsCateAttrAndStock(listadd,listenedit,categorynameArr1,categorynameArr2,
                categorynameArr3,goodsId,user,status,stockLogoUrlAdd,stockLogoUrledit);
        }catch(BusinessException e){
            return Response.fail(e.getErrorDesc());
        }catch (Exception e) {
            LOGGER.error("商品属性规格和库存信息录入失败!", e);
            return Response.fail("请您完整填写商品库存相关信息!");
        }
    }
    /**
     * 修改商品第四页面
     * 修改库存信息
     * 读取商品已有属性 和已有属性下属规格
     * 以及根据属性规格组合 排列 组合 库存列表  此处本请求未实现 在别处请求
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/findGoodsCateAttrAndStockForEdit", method = RequestMethod.POST)
    public Response findGoodsCateAttrAndStockForEdit(HttpServletRequest request) {
        try{
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            return goodsAttrService.findGoodsCateAttrAndStockForEdit(Long.parseLong(goodsId));
        }catch(Exception e){
            return Response.fail("刷新商品库存和属性信息失败！");
        }
    }
    /**
     * 根据属性规格组合 排列 组合 列表
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flushtableattrEditlist", method = RequestMethod.POST)
    public ResponsePageBody<GoodsStockInfoEntity> flushtableattrEditlist(HttpServletRequest request) {
        ResponsePageBody<GoodsStockInfoEntity> respBody = new ResponsePageBody<GoodsStockInfoEntity>();
        try {
            // 获取分页数据
            Page page = new Page();
            String pageNo = HttpWebUtils.getValue(request, "page");
            String pageSize = HttpWebUtils.getValue(request, "rows");
            if(!StringUtils.isAnyBlank(pageNo,pageSize)){
                Integer pageNoNum = Integer.parseInt(pageNo);
                Integer pageSizeNum = Integer.parseInt(pageSize);
                page.setPage(pageNoNum <= 0 ? 1 : pageNoNum);
                page.setLimit(pageSizeNum <= 0 ? 1 : pageSizeNum);
            }
            // 获取页面查询条件
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            //修改的 未改类目
            String categoryname1 = HttpWebUtils.getValue(request, "categorynameArr1");
            String categoryname2 = HttpWebUtils.getValue(request, "categorynameArr2");
            String categoryname3 = HttpWebUtils.getValue(request, "categorynameArr3");
            //修改的 改类目
            String category1 = HttpWebUtils.getValue(request, "categoryname1");
            String category2 = HttpWebUtils.getValue(request, "categoryname2");
            String category3 = HttpWebUtils.getValue(request, "categoryname3");
            String status = HttpWebUtils.getValue(request, "status");
            // 获取分页结果返回给页面
            PaginationManage<GoodsStockInfoEntity> pagination = goodsAttrService.flushtableattrEditlist(goodsId,
                    categoryname1,categoryname2,categoryname3,category1,category2,category3,page,status);
            if (pagination == null||pagination.getDataList()==null||pagination.getDataList().size()==0) {
                //增加空规格库存
                respBody.setTotal(1);
                List<GoodsStockInfoEntity> arr = new ArrayList<GoodsStockInfoEntity>();
                GoodsStockInfoEntity en = new GoodsStockInfoEntity();
                en.setGoodsSkuAttr(" ");
                arr.add(en);
                respBody.setRows(arr);
            }else{
                respBody.setRows(pagination.getDataList());
                respBody.setTotal(pagination.getTotalCount());
            }
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            respBody.setMsg("根据商品 ID 属性规格组合 排列 组合 列表 失败");
        }
        return respBody;
    }
    /**
     * 修改库存
     * INPUT按钮失焦事件 刷新规格库存表失败
     * 直接保存规格    只对规格做修改操作(修改是同步修改库存)！！  不新增。。
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/createTableByCateEdit", method = RequestMethod.POST)
    public Response createTableByCateEdit(HttpServletRequest request) {
        try{
            String attrValId = HttpWebUtils.getValue(request, "attrValId");
            String attrId = HttpWebUtils.getValue(request, "attrId");
            String attrVal = HttpWebUtils.getValue(request, "attrVal");
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            return goodsAttrService.createTableByCateEdit(attrValId,attrId,attrVal,goodsId);
        }catch(BusinessException e){
            return Response.fail(e.getErrorDesc());
        }catch(Exception e){//输入框失焦事件 刷新规格库存表失败！
            return Response.fail("商品规格名称输入有误，请重新输入!");
        }
    }
    /**
     * 修改商品 保存库存 批量保存  商品的属性规格 和库存信息（无规格）
     * 并且保存规格  ，只保存新增的规格   已有规格修改，在上一个方法直接修改完毕
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/editsaveGoodsCateAttrAndStock", method = RequestMethod.POST)
    @LogAnnotion(operationType = "修改商品 保存库存（无规格）", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public Response editsaveGoodsCateAttrAndStock(HttpServletRequest request) {
        try{                                                      
            String[] categorynameArr1 = request.getParameterValues("categorynameArr1");//HttpWebUtils.getValue(request, "categorynameArr1");
            String[] categorynameArr2 = request.getParameterValues("categorynameArr2");//HttpWebUtils.getValue(request, "categorynameArr2");
            String[] categorynameArr3 = request.getParameterValues("categorynameArr3");//HttpWebUtils.getValue(request, "categorynameArr3");
            String[] goodsStock = request.getParameterValues("goodsStock");//HttpWebUtils.getValue(request, "goodsStock");
            String[] stockLogo = request.getParameterValues("stockLogo");//HttpWebUtils.getValue(request, "stockLogo");
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            List<GoodsStockInfoEntity> list = JSONObject.parseObject(goodsStock[0], new TypeReference<List<GoodsStockInfoEntity>>(){});
            List<GoodsStockInfoEntity> listUrledit = JSONObject.parseObject(stockLogo[0], new TypeReference<List<GoodsStockInfoEntity>>(){});
            String user = SpringSecurityUtils.getLoginUserDetails().getUsername();
            return goodsAttrService.editsaveGoodsCateAttrAndStock(list,Long.parseLong(goodsId),user,categorynameArr1,categorynameArr2,categorynameArr3,listUrledit);
        }catch(BusinessException e){
            return Response.fail(e.getErrorDesc());
        }catch (Exception e) {
            LOGGER.error("商品属性规格和库存信息录入失败!", e);
            return Response.fail("请您完整填写商品库存相关信息!");
        }
    }
    /**
     * 校验商品是否无库存
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkoutshock", method = RequestMethod.POST)
    @LogAnnotion(operationType = "查询商品是否无库存", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public Response checkoutshock(HttpServletRequest request) {
        try{
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            return goodsAttrService.checkoutshock(Long.parseLong(goodsId));
        }catch (Exception e) {
            LOGGER.error("校验商品是否无库存失败!", e);
            return Response.fail("2");
        }
    }
    /**
     * 新增商品  第一条属性规格名称集合  刷新规格（缩略图）表格
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/tableattr", method = RequestMethod.POST)
    public ResponsePageBody<StockInfoFileModel> tableattr(HttpServletRequest request) {
        try{
            String arrten = HttpWebUtils.getValue(request, "arrten");
            return goodsAttrService.tableattr(arrten);
        }catch (Exception e) {
            LOGGER.error("商品属性下拉框载入失败!", e);
            return null;
        }
    }
    /**
     * 修改商品（类目更新 库存删除 与上个方法逻辑相同）  第一条属性规格名称集合  刷新规格（缩略图）表格
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/tableattrEdit", method = RequestMethod.POST)
    public ResponsePageBody<GoodsStockInfoEntity> tableattrEdit(HttpServletRequest request) {
        try{
            String arrten = HttpWebUtils.getValue(request, "arrten");
            String goodsId = HttpWebUtils.getValue(request, "goodsId");
            return goodsAttrService.tableattrEdit(arrten,goodsId);
        }catch (Exception e) {
            LOGGER.error("商品属性下拉框载入失败!", e);
            return null;
        }
    }
}