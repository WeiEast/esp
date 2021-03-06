package com.apass.esp.web.goods;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.apass.esp.domain.Response;
import com.apass.esp.domain.dto.goods.StockInfoFileModel;
import com.apass.esp.domain.entity.goods.GoodsStockInfoEntity;
import com.apass.esp.service.goods.GoodsStockInfoService;
import com.apass.esp.utils.FileUtilsCommons;
import com.apass.esp.utils.ImageTools;
import com.apass.esp.utils.PaginationManage;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.log.LogAnnotion;
import com.apass.gfb.framework.log.LogValueTypeEnum;
import com.apass.gfb.framework.security.toolkit.SpringSecurityUtils;
import com.apass.gfb.framework.utils.BaseConstants.CommonCode;
import com.apass.gfb.framework.utils.HttpWebUtils;
import com.apass.gfb.framework.utils.RandomUtils;
/**
 * 商品库存
 * 
 * @author zwd
 * @version 1.0
 * @date 2016年12月21日
 */
@Controller
@RequestMapping("/application/goods/stockinfo")
public class GoodsStockInfoController {
    private static final Logger   LOGGER = LoggerFactory.getLogger(GoodsStockInfoController.class);
    @Autowired
    private GoodsStockInfoService goodsStockInfoService;
    @Value("${nfs.rootPath}")
    private String                rootPath;
    @Value("${nfs.goods}")
    private String                nfsGoods;
    /**
     * 查询库存
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("unused")
    @ResponseBody
    @RequestMapping("/pagelist")
    public ResponsePageBody<GoodsStockInfoEntity> handlePageList(HttpServletRequest request) {
        ResponsePageBody<GoodsStockInfoEntity> respBody = new ResponsePageBody<GoodsStockInfoEntity>();
        try {
            String pageNo = HttpWebUtils.getValue(request, "page");
            String pageSize = HttpWebUtils.getValue(request, "rows");
            String id = HttpWebUtils.getValue(request, "goodsId");
            String source = HttpWebUtils.getValue(request, "source");
            GoodsStockInfoEntity entity = new GoodsStockInfoEntity();
            if(StringUtils.isNotBlank(id)){
                entity.setGoodsId(Long.valueOf(id));
            }
//            List<GoodsStockSkuDto> goodsStockSkuInfos = goodsStockInfoService.getGoodsStockSkuInfo(Long.valueOf(id));
//
//            if (goodsStockSkuInfos!=null){
//                entity.setId(goodsStockSkuInfos.get(0).getGoodsStockId());
//            }
            PaginationManage<GoodsStockInfoEntity> pagination = goodsStockInfoService.pageList(entity, pageNo,
                pageSize);

            respBody.setTotal(pagination.getTotalCount());
            respBody.setRows(pagination.getDataList());
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            LOGGER.error("商品库存查询失败", e);
            respBody.setMsg("商品库存查询失败");
        }
        return respBody;
    }
    /**
     * 新增库存
     * 
     * @param stockInfo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @LogAnnotion(operationType = "新增库存", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response add(@ModelAttribute("stockInfoFileModel") StockInfoFileModel stockInfo) {
        GoodsStockInfoEntity goodsStockInfoEntity = new GoodsStockInfoEntity();
        goodsStockInfoEntity.setGoodsId(stockInfo.getAddstockInfogoodsId());
        String goodsSkuAttr = stockInfo.getGoodsSkuAttr();
        PaginationManage<GoodsStockInfoEntity> list = null;
        try {
            list = goodsStockInfoService.pageList(goodsStockInfoEntity, "0", "1000");
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        List<GoodsStockInfoEntity> dataList = list.getDataList();
        if (!dataList.isEmpty()) {
            if (list.getDataList().size() > 5) {
                return Response.fail("目前只允许添加6种库存！");
            }

            for (GoodsStockInfoEntity goodsStockInfoEntity2 : dataList) {
                if (goodsStockInfoEntity2.getGoodsSkuAttr().equals(goodsSkuAttr)) {
                    return Response.fail("商品规格:" + goodsSkuAttr + "，已经存在，不能重复添加");
                }

            }
        }
        try {
            MultipartFile file = stockInfo.getStockLogoFile();
            String imgType = ImageTools.getImgType(file);
            String fileDiName = RandomUtils.getRandom(10);
            String fileName = "stocklogo_"+ fileDiName + "." + imgType;
            String url = nfsGoods + stockInfo.getAddstockInfogoodsId() + "/" + fileName;

            /**
             * 缩略图校验
             */
            boolean checkSiftGoodsImgSize = ImageTools.checkGoodsLogoImgSize(file);// 尺寸
            boolean checkImgType = ImageTools.checkImgType(file);// 类型
            int size = file.getInputStream().available();// 大小

            if (!(checkSiftGoodsImgSize && checkImgType)) {// 130*130px;;
                                                               // .png,.jpg
                file.getInputStream().close();
                return Response.fail("文件尺寸不符,上传图片尺寸必须是宽：130px,高：130px,格式：.jpg,.png");
            } else if (size > 1024 * 300) {
                file.getInputStream().close();
                return Response.fail("文件不能大于300kb!");
            }
            //上传文件
            FileUtilsCommons.uploadFilesUtil(rootPath, url, stockInfo.getStockLogoFile());
            /**
             * 保存对象
             */
            GoodsStockInfoEntity entity = new GoodsStockInfoEntity();
            entity.setGoodsId(Long.valueOf(stockInfo.getAddstockInfogoodsId()));
            entity.setGoodsSkuAttr(goodsSkuAttr);
            entity.setGoodsPrice(new BigDecimal(stockInfo.getGoodsPrice()));
            entity.setMarketPrice(new BigDecimal(stockInfo.getMarketPrice()));
            entity.setGoodsCostPrice(new BigDecimal(stockInfo.getGoodsCostPrice()));
            entity.setStockTotalAmt(Long.valueOf(stockInfo.getStockTotalAmt()));// 总库存
            entity.setStockCurrAmt(Long.valueOf(stockInfo.getStockTotalAmt()));// 当前库存
            entity.setStockLogo(url);
            String goodsCompareUrl = stockInfo.getGoodsCompareUrl();
            String goodsCompareUrl2 = stockInfo.getGoodsCompareUrl2();
            if(!StringUtils.isAnyBlank(goodsCompareUrl,goodsCompareUrl2)){
                if (goodsCompareUrl.length() > 255 || goodsCompareUrl2.length() > 255) {
                    return Response.fail("比价链接地址过长，每个地址长度必须小于255个字符");
                }
            }
            entity.setGoodsCompareUrl(goodsCompareUrl);
            entity.setGoodsCompareUrl2(goodsCompareUrl2);

            entity.setCreateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
            entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
            Integer count = goodsStockInfoService.insert(entity);
            if (count == 1) {
                return Response.success("success");
            } else {
                return Response.fail("添加库存失败！");
            }
        } catch (Exception e) {
            LOGGER.error("上传logo缩略图失败!", e);
            return Response.fail("上传logo缩略图失败!");
        }
    }
    /**
     * 新增库存  上传缩略图  返回该缩略图URL给前端。
     * 
     * @param stockInfo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/addForLogo", method = RequestMethod.POST)
    @LogAnnotion(operationType = "新增库存", valueType = LogValueTypeEnum.VALUE_DTO)
    public Response addForLogo(@ModelAttribute("stockInfoFileModel") StockInfoFileModel stockInfo) {
        InputStream is = null;
        try {
            MultipartFile file = stockInfo.getStockLogoFile();
            String imgType = ImageTools.getImgType(file);
            String fileDiName = RandomUtils.getRandom(10);
            String fileName = "stocklogo_"+ fileDiName + "." + imgType;
            Long goodsId = stockInfo.getAddstockInfogoodsId();
            if(goodsId==null){
                goodsId = stockInfo.getEditaddstockInfogoodsId();
            }
            String url = nfsGoods + goodsId + "/" + fileName;
            //缩略图校验
            if (!StringUtils.isBlank(file.getOriginalFilename())) {
                boolean checkSiftGoodsImgSize = ImageTools.checkGoodsLogoImgSize(file);// 尺寸
                boolean checkImgType = ImageTools.checkImgType(file);// 类型
                int size = file.getInputStream().available();// 大小
                is = file.getInputStream();
                if (!(checkSiftGoodsImgSize && checkImgType)) {// 130*130px;// .png,.jpg
                    return Response.fail("文件尺寸不符,上传图片尺寸必须是宽：130px,高：130px,格式：.jpg,.png");
                } else if (size > 1024 * 300) {
                    return Response.fail("文件不能大于300kb!");
                }
                //上传文件
                FileUtilsCommons.uploadFilesUtil(rootPath, url, stockInfo.getStockLogoFile());
            }
            return Response.success("success", url);
        } catch (Exception e) {
            LOGGER.error("上传logo缩略图失败!", e);
            return Response.fail("上传logo缩略图失败!");
        }finally{
            try {
                if(is!=null){
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }
    /**
     * 加库存
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/editStockinfo")
    @LogAnnotion(operationType = "商品修改库存", valueType = LogValueTypeEnum.VALUE_REQUEST)
    public Response editStockinfo(HttpServletRequest request) {
        String goodsSkuAttr = HttpWebUtils.getValue(request, "goodsSkuAttr");
        String id = HttpWebUtils.getValue(request, "id");
        String addAmt = HttpWebUtils.getValue(request, "addAmt");
        String goodsCostPrice = HttpWebUtils.getValue(request, "goodsCostPrice");
        String marketPrice = HttpWebUtils.getValue(request, "marketPrice");
        String goodsPrice = HttpWebUtils.getValue(request, "goodsPrice");
//        String goodsCompareUrl = HttpWebUtils.getValue(request, "goodsCompareUrl");
//        String goodsCompareUrl2 = HttpWebUtils.getValue(request, "goodsCompareUrl2");
//        if (!StringUtils.isAnyBlank(goodsCompareUrl,goodsCompareUrl2)) {
//            if (goodsCompareUrl.length() > 255 || goodsCompareUrl2.length() > 255) {
//                return Response.fail("比价链接地址过长，每个地址长度必须小于255个字符");
//            }
//        }

        GoodsStockInfoEntity entity = new GoodsStockInfoEntity();
        entity.setId(Long.valueOf(id));
        if (!StringUtils.isBlank(goodsSkuAttr)) {
            entity.setGoodsSkuAttr(goodsSkuAttr);
        }
        if (!StringUtils.isBlank(addAmt)) {
            entity.setStockTotalAmt(Long.valueOf(addAmt));// 总库存
            entity.setStockCurrAmt(Long.valueOf(addAmt));// 当前库存
        }
        if (!StringUtils.isBlank(goodsCostPrice)) {
            entity.setGoodsCostPrice(new BigDecimal(goodsCostPrice));
        }
        if (!StringUtils.isBlank(marketPrice)) {
            entity.setMarketPrice(new BigDecimal(marketPrice));
        }
        if (!StringUtils.isBlank(goodsPrice)) {
            entity.setGoodsPrice(new BigDecimal(goodsPrice));
        }
//        if (goodsCompareUrl != null) {
//            entity.setGoodsCompareUrl(goodsCompareUrl);
//        }
//        if (goodsCompareUrl2 != null) {
//            entity.setGoodsCompareUrl2(goodsCompareUrl2);
//        }
        entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
        goodsStockInfoService.updateService(entity);
        //根据库存的Id获取库存信息，然后根据goodsid获取
        GoodsStockInfoEntity stock = goodsStockInfoService.goodsStockInfoEntityByStockId(entity.getId());
        return Response.success("success");
    }
    /**
     * 修改商品  修改库存  LOGO重上传
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/upstocklogoFile", method = RequestMethod.POST)
    public Response upStocklogoFile(@ModelAttribute("StockInfoFileModel") StockInfoFileModel stockInfo) {
        try {
            MultipartFile file = stockInfo.getStockLogoFile();
            Long stockinfoId = stockInfo.getEditStockinfoIdInForm();
            Long goodsId = goodsStockInfoService.goodsStockInfoEntityByStockId(stockinfoId).getGoodsId();
            String imgType = ImageTools.getImgType(file);
            String fileDiName = RandomUtils.getRandom(10);
            String fileName = "stocklogo_" + fileDiName + "." + imgType;
            String url = nfsGoods + goodsId + "/" + fileName;
            // 图片验证
            boolean checkLogoImgSize = ImageTools.checkGoodsLogoImgSize(file);// 尺寸
            boolean checkImgType = ImageTools.checkImgType(file);// 类型
            int size = file.getInputStream().available();
            if (!(checkLogoImgSize && checkImgType)) {
                file.getInputStream().close();// 254*320px;.jpg .png
                return Response.fail("文件尺寸不符,上传图片尺寸必须是宽：130px,高：130px,格式：.jpg,.png", url);
            } else if (size > 1024 * 300) {
                file.getInputStream().close();
                return Response.fail("文件不能大于300kb!", url);
            }
            //上传文件
            FileUtilsCommons.uploadFilesUtil(rootPath, url, stockInfo.getStockLogoFile());
            // 保存url到数据库
            GoodsStockInfoEntity entity = new GoodsStockInfoEntity();
            entity.setStockLogo(url);
            entity.setGoodsId(goodsId);
            entity.setId(stockinfoId);
            entity.setUpdateUser(SpringSecurityUtils.getLoginUserDetails().getUsername());
            goodsStockInfoService.update(entity);
            return Response.success("更新缩略图成功", goodsStockInfoService.goodsStockInfoEntityByStockId(stockinfoId));
        } catch (Exception e) {
            LOGGER.error("上传logo失败!", e);
            return Response.fail("上传logo失败!");
        }
    }
}