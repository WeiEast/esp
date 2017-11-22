package com.apass.esp.service.wz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apass.esp.domain.entity.jd.JdProductState;
import com.apass.esp.domain.entity.jd.JdSimilarSku;
import com.apass.esp.third.party.jd.entity.base.Region;
import com.apass.esp.third.party.jd.entity.product.Product;
import com.apass.esp.third.party.weizhi.client.WeiZhiProductApiClient;
import com.apass.esp.third.party.weizhi.entity.AreaLimitEntity;
import com.apass.esp.third.party.weizhi.entity.Category;
import com.apass.esp.third.party.weizhi.entity.CategoryPage;
import com.apass.esp.third.party.weizhi.entity.CheckSale;
import com.apass.esp.third.party.weizhi.entity.GoodsStock;
import com.apass.esp.third.party.weizhi.entity.StockNum;
import com.apass.esp.third.party.weizhi.entity.WZCheckSale;
import com.apass.esp.third.party.weizhi.entity.WZJdSimilarSku;
import com.apass.esp.third.party.weizhi.entity.WzPicture;
import com.apass.esp.third.party.weizhi.entity.WzSkuListPage;
import com.apass.esp.third.party.weizhi.entity.WzSkuPicture;

@Service
public class WeiZhiProductService {
	@Autowired
	private WeiZhiProductApiClient weiZhiProductApiClient;
	
	/**
	 * 获取微知商品详情信息
	 * @return
	 * @throws Exception
	 */
	public Product getWeiZhiProductDetail(String sku) throws Exception {
		return weiZhiProductApiClient.getWeiZhiProductDetail(sku);
	}

	/**
	 * 获取商品上下架状态接口(单个商品) true:上架；false:下架
	 * @throws Exception 
	 */
	public Boolean getWeiZhiProductSkuState(String sku) throws Exception{
		if (StringUtils.isNotEmpty(sku)) {
			List<JdProductState> wzProductState = weiZhiProductApiClient.getWeiZhiProductSkuState(sku);
			if (null != wzProductState && wzProductState.size() == 1) {
				Integer state = wzProductState.get(0).getState();
				if (state == 1) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取商品上下架状态接口(多个商品，最多20个)
	 */
	public List<JdProductState> getWeiZhiProductSkuStateList(List<String> skus) throws Exception {
		List<JdProductState> wzProductState = new ArrayList<>();
		if (null != skus && skus.size() > 0) {
			String skuString = skus.get(0);
			int len = 0;
			if (skus.size() <= 20) {
				len = skus.size();
			} else {
				len = 20;
			}
			for (int i = 1; i < len; i++) {
				skuString = skuString + "," + skus.get(i);
			}
			wzProductState = weiZhiProductApiClient.getWeiZhiProductSkuState(skuString);
		}
		return wzProductState;
	}
	/**
	 * 查询一级分类列表信息接口
	 */
	public List<Category> getWeiZhiFirstCategorys() throws Exception {
		List<Category> categoryList=new ArrayList<>();
		CategoryPage  firstCategorys = weiZhiProductApiClient.getWeiZhiGetCategorys(1,20,1,0);
		if(null !=firstCategorys){
			categoryList=firstCategorys.getCategorys();
			int pageNo=firstCategorys.getPageNo();
			int pageSize=firstCategorys.getPageSize();
			int totalRows=firstCategorys.getTotalRows();
		}
		return categoryList;
	}
	/**
	 * 查询二级分类列表信息接口
	 */
	public List<Category> getWeiZhiSecondCategorys() throws Exception {
		List<Category> categoryList=new ArrayList<>();
		CategoryPage  secondCategorys = weiZhiProductApiClient.getWeiZhiGetCategorys(1,20,2,670);
		if(null !=secondCategorys){
			categoryList=secondCategorys.getCategorys();
			int pageNo=secondCategorys.getPageNo();
			int pageSize=secondCategorys.getPageSize();
			int totalRows=secondCategorys.getTotalRows();
		}
		return categoryList;
	}
	
	/**
	 * 查询三级分类列表信息接口
	 */
	public List<Category> getWeiZhiThirdCategorys() throws Exception {
		List<Category> categoryList=new ArrayList<>();
		CategoryPage  thirdCategorys = weiZhiProductApiClient.getWeiZhiGetCategorys(1,20,3,671);
		if(null !=thirdCategorys){
			categoryList=thirdCategorys.getCategorys();
			int pageNo=thirdCategorys.getPageNo();
			int pageSize=thirdCategorys.getPageSize();
			int totalRows=thirdCategorys.getTotalRows();
		}
		return categoryList;
	}
	/**
	 *获取分类商品编号接口
	 */
	public List<String> getWeiZhiGetSku() throws Exception {
		List<String> skuIdList=new ArrayList<>();
		WzSkuListPage  wzSkuListPage = weiZhiProductApiClient.getWeiZhiGetSku(1,20,672+"");
		if(null !=wzSkuListPage){
			skuIdList=wzSkuListPage.getSkuIds();
			int pageNo=wzSkuListPage.getPageNo();
			int pageSize=wzSkuListPage.getPageSize();
			int totalRows=wzSkuListPage.getTotalRows();
		}
		return skuIdList;
	}

	/**
	 * 获取所有图片信息
	 */
	public List<WzSkuPicture> getWeiZhiProductSkuImage(String sku) throws Exception {
		List<WzSkuPicture> list = new ArrayList<>();
		List<Map<String, List<WzPicture>>> map = weiZhiProductApiClient.getWeiZhiProductSkuImage(sku);
		for (Map<String, List<WzPicture>> map2 : map) {
			for (Map.Entry<String, List<WzPicture>> entry : map2.entrySet()) {
				WzSkuPicture wzSkuPicture = new WzSkuPicture();
				String skuStrign = entry.getKey();
				wzSkuPicture.setSku(skuStrign);
				List<WzPicture> wzPicturelist = entry.getValue();
				wzSkuPicture.setWzPicturelist(wzPicturelist);
				list.add(wzSkuPicture);
			}
		}
		return list;
	}

	/**
	 * 商品区域购买限制查询(单个商品查询)
	 */
	public Boolean getWeiZhiCheckAreaLimit(String skuId, Region region) throws Exception {
		List<AreaLimitEntity> areaLimitEntityList = weiZhiProductApiClient.getWeiZhiCheckAreaLimit(skuId, region);
		if (null != areaLimitEntityList && areaLimitEntityList.size() > 0) {
			AreaLimitEntity areaLimitEntity = areaLimitEntityList.get(0);
			return areaLimitEntity.getIsAreaRestrict();
		}
		return false;
	}
	/**
	 * 商品区域购买限制查询(多个商品查询)
	 */
	public List<AreaLimitEntity> getWeiZhiCheckAreaLimitList(List<String> skuIdList, Region region) throws Exception {
		StringBuilder sb=new StringBuilder();
		if(null !=skuIdList && skuIdList.size()>0){
			int legth=0;
			if(skuIdList.size()<=20){
				legth=skuIdList.size();
			}else{
				legth=20;
			}
			for(int i=0;i<legth;i++){
				sb.append(skuIdList.get(i));
				sb.append(",");
			}
		}
		List<AreaLimitEntity> areaLimitEntityList = weiZhiProductApiClient.getWeiZhiCheckAreaLimit(sb.toString(), region);
		return areaLimitEntityList;
	}
	/**
	 * 商品可售验证接口（单个商品验证）
	 * @param skuIds
	 * @return
	 * @throws Exception
	 */
	public Boolean getWeiZhiCheckSale(String skuIds) throws Exception {
		Boolean falge = false;
		CheckSale checkSale = weiZhiProductApiClient.getWeiZhiCheckSale(skuIds);
		if (null != checkSale.getResult() && checkSale.getResult().size() > 0) {
			WZCheckSale wZCheckSale = checkSale.getResult().get(0);
			if (1 == wZCheckSale.getSaleState()) {
				falge = true;
			}
		}
		return falge;
	}
	/**
	 * 商品可售验证接口(多个商品验证)
	 * @param skuIds
	 * @return
	 * @throws Exception
	 */
	public List<WZCheckSale> getWeiZhiCheckSaleList(List<String> skuIdList) throws Exception {
//		StringBuffer skuIds=new StringBuffer();
//		for (String string : skuIdList) {
//			skuIds.append(string);
//			skuIds.append(",");
//		}
		String skuIds = StringUtils.join(skuIdList,",");
		CheckSale checkSale=weiZhiProductApiClient.getWeiZhiCheckSale(skuIds);
		List<WZCheckSale>  list=checkSale.getResult();
		return list;
	}
	/**
	 * 同类商品查询
	 */
	public List<JdSimilarSku> getWeiZhiSimilarSku(String skuId) throws Exception {
		WZJdSimilarSku wZJdSimilarSku=weiZhiProductApiClient.getWeiZhiSimilarSku(skuId);
		List<JdSimilarSku> list=wZJdSimilarSku.getResult();
		return list;
	}
	/**
	 * 统一余额查询接口
	 * @param skuId
	 * @throws Exception
	 */
	public int  getWeiZhiGetBalance() throws Exception {
		return weiZhiProductApiClient.getWeiZhiGetBalance();
	}
	/**
	 * 微知批量获取库存接口
	 */
	public List<GoodsStock> getNewStockById(List<StockNum> skuNums, Region region) throws Exception {
		return weiZhiProductApiClient.getNewStockById(skuNums, region);
	}
}
