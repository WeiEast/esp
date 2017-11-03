package com.apass.esp.mapper;

import java.util.List;

import com.apass.esp.domain.entity.GoodsAttrVal;
import com.apass.gfb.framework.mybatis.GenericMapper;

/**
 * Created by jie.xu on 17/10/27.
 */
public interface GoodsAttrValMapper  extends GenericMapper<GoodsAttrVal, Long> {
	/**
	 * 根据goodsId查询商品的规格
	 * @param goodsId
	 * @return
	 */
	List<GoodsAttrVal> queryGoodsAttrValsByGoodsId(Long goodsId);
	
	/**
	 * 根据goodsId,attrId查询商品的规格详情
	 * @param goodsId
	 * @return
	 */
	List<GoodsAttrVal> queryByGoodsIdAndAttrId(Long goodsId,Long attrId);
}
