package com.apass.esp.domain.dto.goods;
import org.springframework.web.multipart.MultipartFile;
/**
 * 新增库存 上传图片
 * @author zhanwendong
 *
 */
public class StockInfoFileModel {
	private Long addstockInfogoodsId;//新增商品新增库存上传接收goodsId
	private Long editaddstockInfogoodsId;//修改商品新增库存上传接收goodsId
	private Long editStockinfoIdInForm;//修改商品修改库存上传接收库存Id用于LOGO重上传
	private String attrnameByAfter;//属性规格名称排列组合新名称
	private String goodsSkuAttr;
	private String goodsPrice;//售价
	private String marketPrice;//市场价
	private String goodsCostPrice;//成本价
	private String stockTotalAmt;//库存
	private String stockAmt;//库存剩余
    private MultipartFile stockLogoFile;
    private String stockLogo;//缩略图URL
	private String goodsCompareUrl;//比价链接1
	private String goodsCompareUrl2;//比价链接2
	public Long getAddstockInfogoodsId() {
        return addstockInfogoodsId;
    }
    public void setAddstockInfogoodsId(Long addstockInfogoodsId) {
        this.addstockInfogoodsId = addstockInfogoodsId;
    }
    public String getAttrnameByAfter() {
        return attrnameByAfter;
    }
    public void setAttrnameByAfter(String attrnameByAfter) {
        this.attrnameByAfter = attrnameByAfter;
    }
	public String getGoodsCompareUrl() {
		return goodsCompareUrl;
	}
	public void setGoodsCompareUrl(String goodsCompareUrl) {
		this.goodsCompareUrl = goodsCompareUrl;
	}
	public String getGoodsSkuAttr() {
		return goodsSkuAttr;
	}
	public void setGoodsSkuAttr(String goodsSkuAttr) {
		this.goodsSkuAttr = goodsSkuAttr;
	}
	public String getGoodsPrice() {
		return goodsPrice;
	}
	public void setGoodsPrice(String goodsPrice) {
		this.goodsPrice = goodsPrice;
	}
	public String getMarketPrice() {
		return marketPrice;
	}
	public void setMarketPrice(String marketPrice) {
		this.marketPrice = marketPrice;
	}
	public String getStockTotalAmt() {
		return stockTotalAmt;
	}
	public void setStockTotalAmt(String stockTotalAmt) {
		this.stockTotalAmt = stockTotalAmt;
	}
	public String getGoodsCostPrice() {
		return goodsCostPrice;
	}
	public void setGoodsCostPrice(String goodsCostPrice) {
		this.goodsCostPrice = goodsCostPrice;
	}
	public MultipartFile getStockLogoFile() {
		return stockLogoFile;
	}
	public void setStockLogoFile(MultipartFile stockLogoFile) {
		this.stockLogoFile = stockLogoFile;
	}
	public String getGoodsCompareUrl2() {
        return goodsCompareUrl2;
    }
    public void setGoodsCompareUrl2(String goodsCompareUrl2) {
        this.goodsCompareUrl2 = goodsCompareUrl2;
    }
	@Override
	public String toString() {
		return "StockInfoFileModel [addstockInfogoodsId=" + addstockInfogoodsId
				+ ", goodsSkuAttr=" + goodsSkuAttr + ", goodsPrice="
				+ goodsPrice + ", marketPrice=" + marketPrice
				+ ", goodsCompareUrl=" + goodsCompareUrl + ", goodsCompareUrl2="
				+ goodsCompareUrl2 + ", stockTotalAmt=" + stockTotalAmt
				+ ", goodsCostPrice=" + goodsCostPrice + ", stockLogoFile="
				+ stockLogoFile.getName() + "]";
	}
	public StockInfoFileModel() {
        super();
    }
	public StockInfoFileModel(String attrnameByAfter) {
        super();
        this.attrnameByAfter = attrnameByAfter;
    }
    public String getStockAmt() {
        return stockAmt;
    }
    public void setStockAmt(String stockAmt) {
        this.stockAmt = stockAmt;
    }
    public String getStockLogo() {
        return stockLogo;
    }
    public void setStockLogo(String stockLogo) {
        this.stockLogo = stockLogo;
    }
    public Long getEditaddstockInfogoodsId() {
        return editaddstockInfogoodsId;
    }
    public void setEditaddstockInfogoodsId(Long editaddstockInfogoodsId) {
        this.editaddstockInfogoodsId = editaddstockInfogoodsId;
    }
    public Long getEditStockinfoIdInForm() {
        return editStockinfoIdInForm;
    }
    public void setEditStockinfoIdInForm(Long editStockinfoIdInForm) {
        this.editStockinfoIdInForm = editStockinfoIdInForm;
    }
}