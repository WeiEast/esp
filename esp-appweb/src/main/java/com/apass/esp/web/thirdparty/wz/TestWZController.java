package com.apass.esp.web.thirdparty.wz;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.apass.esp.third.party.weizhi.client.WeiZhiAfterSaleApiClient;
import com.apass.esp.third.party.weizhi.entity.aftersale.AfsApplyWeiZhiDto;
import com.apass.esp.third.party.weizhi.entity.aftersale.WeiZhiAfterSaleDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.entity.jd.JdSimilarSku;
import com.apass.esp.service.wz.WeiZhiProductService;
import com.apass.esp.service.wz.WeiZhiTokenService;
import com.apass.esp.third.party.jd.entity.base.Region;
import com.apass.esp.third.party.jd.entity.product.Product;
import com.apass.esp.third.party.weizhi.client.WeiZhiOrderApiClient;
import com.apass.esp.third.party.weizhi.client.WeiZhiPriceApiClient;
import com.apass.esp.third.party.weizhi.entity.AddressInfo;
import com.apass.esp.third.party.weizhi.entity.Category;
import com.apass.esp.third.party.weizhi.entity.GoodsStock;
import com.apass.esp.third.party.weizhi.entity.OrderReq;
import com.apass.esp.third.party.weizhi.entity.PriceSnap;
import com.apass.esp.third.party.weizhi.entity.SkuNum;
import com.apass.esp.third.party.weizhi.entity.StockNum;
import com.apass.esp.third.party.weizhi.entity.WZCheckSale;
import com.apass.esp.third.party.weizhi.entity.WzSkuPicture;
import com.apass.esp.third.party.weizhi.response.WZPriceResponse;
import com.apass.gfb.framework.utils.CommonUtils;
import com.google.common.collect.Lists;

/**
 * @author zengqingshan
 */
@Controller
@RequestMapping("wz")
public class TestWZController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestWZController.class);
	@Autowired
	private WeiZhiTokenService weiZhiTokenService;
	@Autowired
	private WeiZhiProductService weiZhiProductService;
	@Autowired
	private WeiZhiOrderApiClient order;
	@Autowired
	private WeiZhiPriceApiClient price;
	

	@Autowired
	private WeiZhiAfterSaleApiClient weiZhiAfterSaleApiClient;

	@ResponseBody
	@RequestMapping(value = "/getToken", method = RequestMethod.GET)
	public Response testGetToken() {
		try {
			String token = weiZhiTokenService.getToken();
			if (StringUtils.equals("success", token)) {
				return Response.success("微知token获取成功！");
			}
		} catch (Exception e) {
			LOGGER.error("微知token获取出错！");
		}

		return Response.fail("微知token获取失败！");
	}

	/**
	 * 商品详情
	 * 
	 * @param paramMap
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/productDetailQuery", method = RequestMethod.POST)
	public Response productDetailQuery(@RequestBody Map<String, Object> paramMap) {
		String sku = CommonUtils.getValue(paramMap, "sku");// 商品号
		try {
			Product wzProductDetail = weiZhiProductService.getWeiZhiProductDetail(sku);
			return Response.success("微知获取商品详情成功！", wzProductDetail);
		} catch (Exception e) {
			return Response.fail("微知获取商品详情失败！");
		}
	}

	/**
	 * 查询商品是否上下架
	 */
	@RequestMapping(value = "/jdProductStateQuery", method = RequestMethod.POST)
	@ResponseBody
	public Response jdProductStateQuery(@RequestBody Map<String, Object> paramMap) {
		String sku = CommonUtils.getValue(paramMap, "sku");// 商品号
		Boolean result;
		try {
			result = weiZhiProductService.getWeiZhiProductSkuState(sku);
		} catch (Exception e) {
			return Response.fail("查询商品是否上下架失败！");
		}
		return Response.success("查询商品是否上下架成功！", result);
	}

	/**
	 * 查询一级分类列表信息接口
	 */
	@RequestMapping(value = "/getWeiZhiFirstCategorys", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiFirstCategorys(@RequestBody Map<String, Object> paramMap) {
		try {
			List<Category> categorys = weiZhiProductService.getWeiZhiFirstCategorys();
			System.out.println(categorys);
			return Response.success("查询一级分类列表信息接口成功！",categorys);
		} catch (Exception e) {
			return Response.fail("查询一级分类列表信息接口失败！");
		}
	}

	/**
	 * 查询二级分类列表信息接口
	 */
	@RequestMapping(value = "/getWeiZhiSecondCategorys", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiSecondCategorys(@RequestBody Map<String, Object> paramMap) {
		try {
			List<Category> categorys = weiZhiProductService.getWeiZhiSecondCategorys();
			System.out.println(categorys);
			return Response.success("查询二级分类列表信息接口成功！",categorys);
		} catch (Exception e) {
			return Response.fail("查询二级分类列表信息接口失败！");
		}
		
	}

	/**
	 * 查询三级分类列表信息接口
	 */
	@RequestMapping(value = "/getWeiZhiThirdCategorys", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiThirdCategorys(@RequestBody Map<String, Object> paramMap) {
		List<Category> categorys=null;
		try {
			 categorys = weiZhiProductService.getWeiZhiThirdCategorys();
			System.out.println(categorys);
			return Response.success("查询三级分类列表信息接口成功！",categorys);
		} catch (Exception e) {
			return Response.fail("查询三级分类列表信息接口失败！");
		}
	}

	/**
	 * 获取分类商品编号接口
	 */
	@RequestMapping(value = "/getWeiZhiGetSku", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiGetSku(@RequestBody Map<String, Object> paramMap) {
		List<String> categorys=null;
		try {
			 categorys = weiZhiProductService.getWeiZhiGetSku();
			System.out.println(categorys);
			return Response.success("获取分类商品编号接口成功！", categorys);
		} catch (Exception e) {
			return Response.fail("获取分类商品编号接口失败！");
		}
	}

	/**
	 * 获取所有图片信息
	 */
	@RequestMapping(value = "/getWeiZhiProductSkuImage", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiProductSkuImage(@RequestBody Map<String, Object> paramMap) {
		try {
			List<WzSkuPicture> wzSkuPictureList = weiZhiProductService
					.getWeiZhiProductSkuImage(1593516 + "," + 1686504);
			System.out.println(wzSkuPictureList);
			return Response.success("获取所有图片信息成功！",wzSkuPictureList);
		} catch (Exception e) {
			return Response.fail("获取所有图片信息失败！");
		}
	}
	
	/**
	 * 根据skuid，获取微知 和京东的价格
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/price")
	@ResponseBody
	public Response getWzPrice() throws Exception{
		List<String> skuList = Lists.newArrayList();
		skuList.add("1331125");
		List<WZPriceResponse> priceList = price.getWzPrice(skuList);
		return Response.successResponse(priceList);
	}
	
	/**
	 * 6.4 取消订单接口
	 * @throws Exception 
	 */
	@RequestMapping(value = "/cancelOrder")
	@ResponseBody
	public Response cancelOrder() throws Exception {
		return Response.successResponse(order.cancelOrder("2017112115593775"));
	}
	
	/**
	 * 订单反查接口
	 * @throws Exception 
	 */
	@RequestMapping(value = "/selectOrderIdByThirdOrder")
	@ResponseBody
	public Response selectOrderIdByThirdOrder() throws Exception {
		return Response.successResponse(order.selectOrderIdByThirdOrder("11234567890"));
	}
	
	/**
	 * 确认预占库存
	 * @throws Exception 
	 */
	@RequestMapping(value = "/orderOccupyStockConfirm")
	@ResponseBody
	public Response orderOccupyStockConfirm() throws Exception {
		return Response.successResponse(order.orderOccupyStockConfirm("2017111716513561"));
	}
	
	/**
	 * 查询订单信息接口
	 * @throws Exception 
	 */
	@RequestMapping(value = "/selectOrder")
	@ResponseBody
	public Response selectOrder() throws Exception {
		return Response.successResponse(order.selectOrder("2017111716513561"));
	}
	
	/**
	 * 根据订单号，获取物流信息
	 * @throws Exception 
	 */
	@RequestMapping(value = "/orderTrack")
	@ResponseBody
	public Response orderTrack() throws Exception {
		return Response.successResponse(order.orderTrack("2017111716513561"));
	}
	
	/**
	 * 统一下单接口
	 * @throws Exception 
	 */
	@RequestMapping(value = "/unitOrder")
	@ResponseBody
	public Response getWeiZhiUnitOrder() throws Exception {
		OrderReq req = new OrderReq();
		req.setOrderNo("11134567890");
		req.setRemark("hahah");
		
		AddressInfo addressInfo = new AddressInfo();
	    addressInfo.setProvinceId(2);
        addressInfo.setCityId(2815);
        addressInfo.setCountyId(51975);
        addressInfo.setTownId(0);
        addressInfo.setAddress("延安西路726号华敏翰尊大厦3层A-5");
        addressInfo.setReceiver("victorpeng");
        addressInfo.setEmail("jdsupport@apass.cn");
        addressInfo.setMobile("18321017352");
        
        req.setAddressInfo(addressInfo);
       
        List<SkuNum> skuNumList = new ArrayList<>();
        List<PriceSnap> priceSnaps = new ArrayList<>();
        SkuNum skuNum = new SkuNum();
        skuNum.setNum(1);
        skuNum.setSkuId(1331125);
        skuNum.setPrice(new BigDecimal(1763.0));
        skuNumList.add(skuNum);
        req.setSkuNumList(skuNumList);
        
    	PriceSnap priceSnap = new PriceSnap();
    	priceSnap.setSkuId(1331125);
    	priceSnap.setPrice(new BigDecimal(1763.0));
    	priceSnaps.add(priceSnap);
        req.setOrderPriceSnap(priceSnaps);
        
        return Response.successResponse(order.submitOrder(req));
	}

	/**
	 * 商品区域购买限制查询(单个商品查询)
	 */
	@RequestMapping(value = "/checkAreaLimit", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiCheckAreaLimit(@RequestBody Map<String, Object> paramMap) {
		try {
			Region region = new Region();
			Boolean result = weiZhiProductService.getWeiZhiCheckAreaLimit("", region);
			return Response.success("商品区域购买限制查询成功！", result);
		} catch (Exception e) {
			return Response.fail("商品区域购买限制查询失败！");
		}
	}
	/**
	 * 商品可售验证接口
	 */
	@RequestMapping(value = "/checkSale", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiCheckSale(@RequestBody Map<String, Object> paramMap)  {
		try {
			Boolean result = weiZhiProductService.getWeiZhiCheckSale("180389");
			return Response.success("商品可售验证接口成功！", result);
		} catch (Exception e) {
			return Response.fail("商品可售验证接口失败！");
		}
	}

	/**
	 * 服务单保存申请
	 */
	@RequestMapping(value = "/createAfsApply", method = RequestMethod.POST)
	@ResponseBody
	public Response createAfsApply(@RequestBody Map<String, Object> paramMap) {
		try {
			AfsApplyWeiZhiDto AfsApplyWeiZhiDto = new AfsApplyWeiZhiDto();
			WeiZhiAfterSaleDto weiZhiAfterSaleApplyDto = weiZhiAfterSaleApiClient.afterSaleAfsApplyCreate(AfsApplyWeiZhiDto);
			return Response.success("服务单保存申请成功！", weiZhiAfterSaleApplyDto);
		} catch (Exception e) {
			return Response.fail("服务单保存申请失败！");
		}
	}


	/**
	 * 校验某订单中某商品是否可以提交售后服务
	 */
	@RequestMapping(value = "/availableNumberComp", method = RequestMethod.POST)
	@ResponseBody
	public Response getAvailableNumberComp(@RequestBody Map<String, String> paramMap) {
		try {
			WeiZhiAfterSaleDto weiZhiAfterSaleApplyDto = weiZhiAfterSaleApiClient.getAvailableNumberComp(paramMap);

			return Response.success("校验某订单中某商品是否可以提交售后服务成功！", weiZhiAfterSaleApplyDto);
		} catch (Exception e) {
			return Response.fail("校验某订单中某商品是否可以提交售后服务失败！");
		}
	}

	/**
	 * 同类商品查询
	 */
	@RequestMapping(value = "/similarSku", method = RequestMethod.POST)
	@ResponseBody
	public Response getWeiZhiSimilarSku(@RequestBody Map<String, Object> paramMap) throws Exception {
		try {
			List<JdSimilarSku> list = weiZhiProductService.getWeiZhiSimilarSku("1815738");
			return Response.success("同类商品查询成功！", list);
		} catch (Exception e) {
			return Response.fail("同类商品查询失败！");
		}
	}
	/**
	 * 统一余额查询接口
	 * @param skuId
	 * @throws Exception
	 */
	@RequestMapping(value = "/getBalance", method = RequestMethod.GET)
	@ResponseBody
	public Response  getWeiZhiGetBalance() {
		try {
			int price=weiZhiProductService.getWeiZhiGetBalance();
			return Response.success("统一余额查询成功！", price);
		} catch (Exception e) {
			return Response.fail("统一余额查询失败！");
		}
	}

	/**
	 * 微知批量获取库存接口
	 */
	@RequestMapping(value = "/getNewStockById", method = RequestMethod.POST)
	@ResponseBody
	public Response getNewStockById(@RequestBody Map<String, Object> paramMap){
		try {
			List<StockNum> skuNums = new ArrayList<>();
			StockNum stockNum=new StockNum();
			stockNum.setSkuId(Long.parseLong("4163957"));
			stockNum.setNum(1);
			skuNums.add(stockNum);
			Region region = new Region();
			region.setProvinceId(1);
			region.setCityId(0);
			region.setCountyId(0);
			List<GoodsStock> result = weiZhiProductService.getNewStockById(skuNums, region);
			return Response.success("微知批量获取库存成功！", result);
		} catch (Exception e) {
			return Response.fail("微知批量获取库存失败！");
		}

	}
}
