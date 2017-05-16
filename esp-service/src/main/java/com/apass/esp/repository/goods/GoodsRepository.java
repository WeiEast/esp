package com.apass.esp.repository.goods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apass.esp.domain.entity.cart.CartInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsBasicInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsDetailInfoEntity;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.gfb.framework.annotation.MyBatisRepository;
import com.apass.gfb.framework.mybatis.page.Page;
import com.apass.gfb.framework.mybatis.page.Pagination;
import com.apass.gfb.framework.mybatis.support.BaseMybatisRepository;

/**
 * 
 * @description banner信息Repository
 *
 * @author lixining
 * @version $Id: CustomerInfoRepository.java, v 0.1 2015年8月6日 上午10:51:37
 *          lixining Exp $
 */
@MyBatisRepository
public class GoodsRepository extends BaseMybatisRepository<GoodsInfoEntity, Long> {

    public List<GoodsBasicInfoEntity> loadRecommendGoods() {
        return this.getSqlSession().selectList("loadRecommendGoods");
    }

    public Pagination<GoodsInfoEntity> loadGoodsByPages(Page page, GoodsInfoEntity param) {
        return this.page(param, page);
    }

    /**
     * count
     */
    public Integer countGoods(GoodsInfoEntity domain) {
        return getSqlSession().selectOne("countGoods", domain);
    }

    /**
     * 商品信息列表
     * 
     * @param domain
     *            GoodsInfoEntity
     * @param page
     * @return
     */
    public Pagination<GoodsInfoEntity> pageList(GoodsInfoEntity domain, Page page) {
        return this.pageBykey(domain, page, "goodsPageList");

    }

    /**
     * 商品信息列表
     * 
     * @param domain
     *            GoodsInfoEntity
     * @return
     */
    public List<GoodsInfoEntity> pageList(GoodsInfoEntity domain) {
        return getSqlSession().selectList("goodsPageList", domain);
    }

    /**
     * 说明：商品精选列表
     * 
     * @param domain
     * @param page
     * @return
     * @author xiaohai
     * @time：2016年12月29日 下午2:55:57
     */
    public Pagination<GoodsInfoEntity> pageForSiftList(GoodsInfoEntity domain, Page page) {
        return this.pageBykey(domain, page, "pageForSiftList");

    }

    /**
     * 商品基本信息+商户信息+库存信息
     * 
     * @param goodsId
     * @param goodsStockId
     * @return
     */
    public GoodsDetailInfoEntity loadContainGoodsAndGoodsStockAndMerchant(Long goodsId, Long goodsStockId) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("goodsId", goodsId);
        param.put("goodsStockId", goodsStockId);
        return this.getSqlSession().selectOne("loadContainGoodsAndGoodsStockAndMerchant", param);
    }

    public Integer goodsPageListCount() {
        // goodsPageListCount
        return getSqlSession().selectOne("countByGoodsType");
    }

    /**
     * 加载商品列表
     * 
     * @return
     */
    public List<GoodsBasicInfoEntity> loadGoodsList() {
        return this.getSqlSession().selectList("loadGoodsList");
    }

    public Integer updateGoods(GoodsInfoEntity entity) {
        return this.getSqlSession().update("updateGoods", entity);
    }

    /**
     * 同步 购物车商品 勾选标记
     * 
     * @param cartDto
     */
    public void synIsSelect(CartInfoEntity cartDto) {
        this.getSqlSession().update("synIsSelect", cartDto);
    }

    public Integer updateGoodsEdit(GoodsInfoEntity dto) {
        return this.getSqlSession().update("updateGoodsEdit", dto);
    }
    
	/**
	 * 校验商品下架时间，修改商品状态
	 * 
	 * @return
	 */
	public void updateGoodsStatusByDelisttime() {
		this.getSqlSession().update("updateGoodsStatusByDelisttime");
	}
	
	/**
	 * 查询所属分类下属的商品的数量（status!=G03 并且 is_delete !='00'）
	 * @return
	 */
	public int getBelongCategoryGoodsNumber(long id){
    	return this.getSqlSession().selectOne("getBelongCategoryGoodsNumber",id);
    }
}
