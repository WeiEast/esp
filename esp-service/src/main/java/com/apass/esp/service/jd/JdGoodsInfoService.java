package com.apass.esp.service.jd;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.apass.esp.domain.entity.address.AddressInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.entity.jd.JdGoodStock;
import com.apass.esp.domain.entity.jd.JdGoods;
import com.apass.esp.domain.entity.jd.JdGoodsBooks;
import com.apass.esp.domain.entity.jd.JdImage;
import com.apass.esp.domain.entity.jd.JdSaleAttr;
import com.apass.esp.domain.entity.jd.JdSellPrice;
import com.apass.esp.domain.entity.jd.JdSimilarSku;
import com.apass.esp.domain.entity.jd.JdSimilarSkuVo;
import com.apass.esp.domain.enums.JdGoodsImageType;
import com.apass.esp.repository.goods.GoodsRepository;
import com.apass.esp.third.party.jd.client.JdApiResponse;
import com.apass.esp.third.party.jd.client.JdProductApiClient;
import com.apass.esp.third.party.jd.entity.base.Region;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 获取京东商品基础信息（前端展示信息）
 * @author zengqingshan
 */
@Service
public class JdGoodsInfoService {
	@Autowired
    private JdProductApiClient jdProductApiClient;
	@Autowired
	private GoodsRepository goodsRepository;

	/**
	 * 根据商品编号获取商品需要展示前端信息
	 */
	public Map<String, Object> getJdGoodsAllInfoBySku(Long sku) {
		Map<String, Object> map = Maps.newHashMap();
		if (sku.toString().length() == 8) {
			// 查询商品名称（图书音像类目）
			JdGoodsBooks jdGoodsBooks = getJdGoodsBooksInfoBySku(sku);
			map.put("relatedProducts", jdGoodsBooks.getRelatedProducts());
		} else {
			// 查询商品名称
			JdGoods jdGoods = getJdGoodsInfoBySku(sku);
			map.put("goodsName", jdGoods.getName());// 商品名称
			//java字符串转义,把&lt;&gt;转换成<>等字符
            String introduction = jdGoods.getIntroduction().replaceAll("width","width");
			map.put("googsDetail", StringEscapeUtils.unescapeXml(introduction));// 商品详情
		}
		// 查询商品价格
		Collection<Long> skuPrice = new ArrayList<Long>();
		skuPrice.add(sku);
		List<JdSellPrice> jdSellPriceList = getJdSellPriceBySku(skuPrice);
		if (null != jdSellPriceList && jdSellPriceList.size() == 1) {
			map.put("goodsPrice", new DecimalFormat("0.00").format(jdSellPriceList.get(0).getJdPrice()));// 商品价格
		}
		// 查询商品图片
		List<String> JdImagePathList = getJdImagePathListBySku(sku, JdGoodsImageType.TYPEN0.getCode());
		map.put("jdImagePathList", JdImagePathList);
		// 查询商品规格
		List<JdSimilarSku> jdSimilarSkuList = getJdSimilarSkuList(sku);
		map.put("skuId",String.valueOf(sku));
		map.put("jdSimilarSkuList", jdSimilarSkuList);
		map.put("jdSimilarSkuListSize", jdSimilarSkuList.size());
		return map;
	}
	/**
	 * 根据商品编号获取商品需要展示App信息
	 */
	public Map<String, Object> getAppJdGoodsAllInfoBySku(Long sku, List<AddressInfoEntity> AddressInfoEntityList) {
		Map<String, Object> map = Maps.newHashMap();
		if (sku.toString().length() == 8) {
			// 查询商品名称（图书音像类目）
			JdGoodsBooks jdGoodsBooks = getJdGoodsBooksInfoBySku(sku);
			map.put("relatedProducts", jdGoodsBooks.getRelatedProducts());
		} else {
			// 查询商品名称
			JdGoods jdGoods = getJdGoodsInfoBySku(sku);
			map.put("goodsName", jdGoods.getName());// 商品名称
			// java字符串转义,把&lt;&gt;转换成<>等字符
			String introduction = jdGoods.getIntroduction().replaceAll("width", "width");
			map.put("googsDetail", StringEscapeUtils.unescapeXml(introduction));// 商品详情
		}
		// 查看商品的邮费
		// ToDO

		// 查询商品图片
		List<String> JdImagePathList = getJdImagePathListBySku(sku, JdGoodsImageType.TYPEN0.getCode());
		map.put("jdImagePathList", JdImagePathList);

		// 查询商品规格
		TreeSet<String> skusSet = new TreeSet<String>();
		List<JdSimilarSku> jdSimilarSkuList = getJdSimilarSkuList(sku);
		for (JdSimilarSku jdsk : jdSimilarSkuList) {
			List<JdSaleAttr> saleAttrList = jdsk.getSaleAttrList();
			for (JdSaleAttr jdsa : saleAttrList) {
				jdsa.setImagePath("http://img13.360buyimg.com/n4/" + jdsa.getImagePath());
				List<String> skuIds = jdsa.getSkuIds();
				for (int i = 0; i < skuIds.size(); i++) {
					String skuId = skuIds.get(i);
					GoodsInfoEntity gty = goodsRepository.selectGoodsBySkuId(skuId);
					if (null == gty) {// 当改商品的规格中的商品没有关联时移除
						skuIds.remove(i);
					} else {
						skusSet.add(skuId);
					}
				}
			}
		}
		// 获取地址信息
		Region region = new Region();
		for (AddressInfoEntity addressInfoEntity : AddressInfoEntityList) {
			if ("1".equals(addressInfoEntity.getIsDefault())) {
				region.setProvinceId(Integer.parseInt(addressInfoEntity.getProvinceCode()));
				region.setCityId(Integer.parseInt(addressInfoEntity.getCityCode()));
				region.setCountyId(Integer.parseInt(addressInfoEntity.getDistrictCode()));
			}
		}
		// 查询商品规格中的商品的价格和库存
		List<JdSimilarSkuVo> JdSimilarSkuVoList = new ArrayList<>();
		Iterator<String> iterator = skusSet.iterator();
		while (iterator.hasNext()) {
			JdSimilarSkuVo jdSimilarSkuVo = new JdSimilarSkuVo();
			String skuId = iterator.next();
			// 查询商品价格
			Collection<Long> skuPrice = new ArrayList<Long>();
			skuPrice.add(Long.parseLong(skuId));
			List<JdSellPrice> jdSellPriceList = getJdSellPriceBySku(skuPrice);
			JdGoodStock jdGoodStock = stockForListBatget(skuId, region);
			jdSimilarSkuVo.setSkuId(skuId);
			jdSimilarSkuVo.setPrice(jdSellPriceList.get(0).getPrice());
			if ("33".equals(jdGoodStock.getState())) {
				jdSimilarSkuVo.setStockDesc(jdGoodStock.getDesc());
			}
			JdSimilarSkuVoList.add(jdSimilarSkuVo);
		}
		map.put("JdSimilarSkuVoList", JdSimilarSkuVoList);
		map.put("skuId", String.valueOf(sku));
		map.put("jdSimilarSkuList", jdSimilarSkuList);
		map.put("jdSimilarSkuListSize", jdSimilarSkuList.size());
		return map;
	}
	/**
	 * 根据商品编号，获取商品明细信息(sku为8位时为图书音像类目商品)
	 */
	public JdGoodsBooks getJdGoodsBooksInfoBySku(Long sku) {
		if (sku.toString().length() != 8) {
			return null;
		}
		Gson gson = new Gson();
		JdGoodsBooks jdGoodsBooks = new JdGoodsBooks();
		// 查询图书音像类目商品信息
		JdApiResponse<JSONObject> jdGoodsBooksDetail = jdProductApiClient.productDetailQuery(sku);
		if (null != jdGoodsBooksDetail && null != jdGoodsBooksDetail.getResult() && jdGoodsBooksDetail.isSuccess()) {
			jdGoodsBooks = gson.fromJson(jdGoodsBooksDetail.getResult().toString(), JdGoodsBooks.class);
		}
		return jdGoodsBooks;
	}
	/**
	 * 根据商品编号，获取商品明细信息(sku不为8位时为非图书音像类目商品)
	 */
	public JdGoods getJdGoodsInfoBySku(Long sku) {
		if (sku.toString().length() == 8) {
			return null;
		}
		Gson gson = new Gson();
		JdGoods jdGoods = new JdGoods();
		// 查询图书音像类目商品信息
		JdApiResponse<JSONObject> jdGoodsBooksDetail = jdProductApiClient.productDetailQuery(sku);
		if (null != jdGoodsBooksDetail && null != jdGoodsBooksDetail.getResult() && jdGoodsBooksDetail.isSuccess()) {
			jdGoods = gson.fromJson(jdGoodsBooksDetail.getResult().toString(), JdGoods.class);
		}
		return jdGoods;
	}

	/**
	 * 根据商品编号，获取商品价格
	 *
	 * @param sku
	 * @return
	 */
	public List<JdSellPrice> getJdSellPriceBySku(Collection<Long> sku) {
		Gson gson = new Gson();
		List<JdSellPrice> jdSellPriceList = new ArrayList<>();
		JdApiResponse<JSONArray> jdSellPrice = jdProductApiClient.priceSellPriceGet(sku);
		if (null != jdSellPrice && null != jdSellPrice.getResult() && jdSellPrice.isSuccess()) {
			for (int i = 0; i < jdSellPrice.getResult().size(); i++) {
				JdSellPrice jp = gson.fromJson(jdSellPrice.getResult().getString(i), JdSellPrice.class);
				jdSellPriceList.add(jp);
			}
		}
		return jdSellPriceList;
	}
	/**
	 * 查询商品的图片地址信息（根据商品编号和图片类型）
	 * @param sku
	 * @param type{n0(最大图)、n1(350*350px)、n2(160*160px)、n3(130*130px)、n4(100*100px) }
	 * @return
	 */
	public List<String> getJdImagePathListBySku(Long sku, String type) {
		List<Long> skusImage = new ArrayList<>();
		skusImage.add(sku);
		List<String> JdImagePathList = new ArrayList<>();
		JdApiResponse<JSONObject> jdImageResponse = jdProductApiClient.productSkuImageQuery(skusImage);
		if (null != jdImageResponse && null != jdImageResponse.getResult() && jdImageResponse.isSuccess()) {
			Map<String, List<JdImage>> jsonImageResult = JSONObject.parseObject(jdImageResponse.getResult().toString(),
					new TypeReference<Map<String, List<JdImage>>>(){});

			List<JdImage> jdList = jsonImageResult.get(sku.toString());
			for (int i = 0; i < jdList.size(); i++) {
				String path = jdList.get(i).getPath();
				String pathJd = "http://img13.360buyimg.com/" + type + "/" + path;
				JdImagePathList.add(pathJd);
			}
		}
		return JdImagePathList;
	}
	/**
	 * 查询商品的图片信息（根据商品编号）
	 * @param sku
	 * @return
	 */
	public List<JdImage> getJdImageListBySku(Long sku) {
		Gson gson = new Gson();
		List<Long> skusImage = new ArrayList<>();
		skusImage.add(sku);
		List<JdImage> jdList = new ArrayList<>();
		JdApiResponse<JSONObject> jdImageResponse = jdProductApiClient.productSkuImageQuery(skusImage);
		if (null != jdImageResponse && null != jdImageResponse.getResult() && jdImageResponse.isSuccess()) {
			Map<String, List<JdImage>> jsonImageResult = gson.fromJson(jdImageResponse.getResult().toString(),
					new TypeToken<Map<String, List<JdImage>>>() {
					}.getType());
			jdList = jsonImageResult.get(sku);
		}
		return jdList;
	}
	/**
	 * 查询商品的图片信息（根据商品编号(多个sku)）
	 * @param sku
	 * @return
	 */
	public Map<String, List<JdImage>> getJdImageInfoListBySku(List<Long> skus) {
		Gson gson = new Gson();
		Map<String, List<JdImage>> jsonImageResult = new HashMap<>();
		JdApiResponse<JSONObject> jdImageResponse = jdProductApiClient.productSkuImageQuery(skus);
		if (null != jdImageResponse && null != jdImageResponse.getResult() && jdImageResponse.isSuccess()) {
			jsonImageResult = gson.fromJson(jdImageResponse.getResult().toString(),
					new TypeToken<Map<String, List<JdImage>>>() {
					}.getType());
		}
		return jsonImageResult;
	}

	/**
	 * 同类商品查询(根据商品编号sku)
	 *
	 * @return
	 */
	public List<JdSimilarSku> getJdSimilarSkuList(Long sku) {
		Gson gson = new Gson();
		JdApiResponse<JSONArray> jdSimilarResponse = jdProductApiClient.getSimilarSku(sku);
		List<JdSimilarSku> JdSimilarSkuList = new ArrayList<>();
		for (int i = 0; i < jdSimilarResponse.getResult().size(); i++) {
			JdSimilarSku jp = gson.fromJson(jdSimilarResponse.getResult().getString(i), JdSimilarSku.class);
			jp.update(jp.getSaleAttrList());
			JdSimilarSkuList.add(jp);
		}
		return JdSimilarSkuList;
	}

	/**
	 * 查询商品的规格(根据商品sku查询商品本身的规格)
	 * 例如：("颜色":"金色","版本":"全网通")
	 */
	public Map<String,String> getJdGoodsSpecification(Long sku) {
		List<JdSimilarSku> jdSimilarSkuList = getJdSimilarSkuList(sku);
		Map<String, String> map = new HashMap<>();
		for (JdSimilarSku jdSimilarSku : jdSimilarSkuList) {
			String saleName = jdSimilarSku.getSaleName();
			String saleValue = "";
			List<JdSaleAttr> saleAttrList = jdSimilarSku.getSaleAttrList();
			for (JdSaleAttr jdSaleAttr : saleAttrList) {
				List<String> skuIds = jdSaleAttr.getSkuIds();
				for (String skuid : skuIds) {
					if (sku.toString().equals(skuid)) {
						saleValue = jdSaleAttr.getSaleValue();
						break;
					}
				}
			}
			map.put(saleName, saleValue);
		}
		return map;
	}
	/**
	 * 获取商品库存接口
	 */
	public JdGoodStock stockForListBatget(String sku, Region region) {
		Gson gson = new Gson();
		JdGoodStock jdGoodStock = new JdGoodStock();
		JdApiResponse<JSONArray> stockForListBatgetResponse = jdProductApiClient.stockForListBatget(sku, region);
		if (null != stockForListBatgetResponse && null != stockForListBatgetResponse.getResult() && stockForListBatgetResponse.isSuccess()) {
			jdGoodStock = gson.fromJson(stockForListBatgetResponse.getResult().toString(), JdGoodStock.class);
		}
		return jdGoodStock;
	}


}
