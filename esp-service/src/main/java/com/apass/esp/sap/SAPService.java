package com.apass.esp.sap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.apass.esp.domain.entity.ApassTxnAttr;
import com.apass.esp.domain.entity.bill.PurchaseOrderDetail;
import com.apass.esp.domain.entity.bill.PurchaseReturnOrder;
import com.apass.esp.domain.entity.bill.SalesOrderInfo;
import com.apass.esp.domain.entity.bill.SalesOrderPassOrRefund;
import com.apass.esp.domain.entity.bill.TxnOrderInfo;
import com.apass.esp.domain.enums.MerchantCode;
import com.apass.esp.domain.enums.OrderStatus;
import com.apass.esp.domain.enums.RefundStatus;
import com.apass.esp.domain.enums.TxnTypeCode;
import com.apass.esp.service.TxnInfoService;
import com.apass.esp.service.order.OrderService;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.gfb.framework.utils.FTPUtils;
import com.csvreader.CsvWriter;
/**
 * Created by jie.xu on 17/10/16.
 */
@Service
public class SAPService {
    public static final String ZPTMC = "中原项目组（安家趣花）";
    private static final Logger LOG = LoggerFactory.getLogger(SAPService.class);
    @Autowired
    private TxnInfoService txnInfoService;
    @Autowired
    private OrderService orderService;
    /**
     * 上传财物凭证调整（首付款或全额）
     */
    public void sendCaiWuPingZhengCsv(String ip, int port, String username,
                                      String password, String path
    ) {
        try {
            generateCaiWuPingZhengCsv();
            FileInputStream in = new FileInputStream(new File(SAPConstants.CAIWUPINGZHENG_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.CAIWUPINGZHENG_FILE_NAME, in);
        } catch (Exception e) {
            LOG.error("ftp caiwupingzheng csv error", e);
        }
    }
    /**
     * 上传财务凭证调整明细
     */
    public void sendCaiWuPingZhengCsv2(String ip, int port, String username,
                                       String password, String path) {
        try {
            generateCaiWuPingZhengCsv2();
            FileInputStream in = new FileInputStream(new File(SAPConstants.CAIWUPINGZHENG_FILE_PATH2));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.CAIWUPINGZHENG_FILE_NAME2, in);
        } catch (Exception e) {
            LOG.error("ftp caiwupingzheng2 csv error", e);
        }
    }
    /**
     * 首付款或全额（购买退货）流水
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param path
     */
    public void commodityReturnFlow(String ip, int port, String username,
                                            String password, String path
    ) {
        FileInputStream in = null;
        try {
            generatePaymentOrFullPaymentCsv();
            in = new FileInputStream(new File(SAPConstants.PAYMENTORFULLPAYMENT_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.PAYMENTORFULLPAYMENT_FILE_NAME, in);
        } catch (Exception e) {
            LOG.error("ftp commodityreturnflow csv error", e);
        }finally {
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 销售订单(通过,退货)
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param path
     */
    public void salesOrder(String ip, int port, String username,
                               String password, String path
    ) {
        FileInputStream in = null;
        try {
            generateSalesOrderCsv();
            in = new FileInputStream(new File(SAPConstants.SALESORDER_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.SALESORDER_FILE_NAME, in);
        } catch (Exception e) {
            LOG.error("ftp salesOrder csv error", e);
        }finally {
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 销售订单明细
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param path
     */
    public void salesOrderInfo(String ip, int port, String username,
                                    String password, String path
    ) {
        FileInputStream in = null;
        try {
            generateSalesOrderInfoCsv();
            in = new FileInputStream(new File(SAPConstants.SALESORDERINFO_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.SALESORDERINFO_FILE_NAME, in);
        } catch (Exception e) {
            LOG.error("ftp salesOrderInfo csv error", e);
        }finally {
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * VBS业务号对应
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param path
     */
    public void transVBSBusinessNumCvs(String ip, int port, String username, String password, String path) {
        InputStream fis = null;
        try {
            transVBSBusinessNumCvs();
            fis = new FileInputStream(new File(SAPConstants.VBSBUSINESS_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.VBSBUSINESS_FILE_NAME, fis);
        } catch (FileNotFoundException e) {
            LOG.error("ftp VBSBusiness csv file notfound", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                LOG.debug("IO Close error", e);
            }
        }
    }
  	/**
  	 * (采购,退货）流水   现阶段只有采购
  	 * @param ip
  	 * @param port
  	 * @param username
  	 * @param password
  	 * @param path
  	 */
  	public void transPurchaseOrReturnCvs(String ip, int port, String username,String password, String path){
        InputStream fis = null;
        try {
            transPurchaseOrReturnCvs();
            fis = new FileInputStream(new File(SAPConstants.PURCHASEORRETURN_FILE_PATH));
            FTPUtils.uploadFile(ip,port,username,password,path,SAPConstants.PURCHASEORRETURN_FILE_NAME,fis);
        } catch (FileNotFoundException e) {
            LOG.error("ftp PurchaseOrder csv file notfound",e);
        }finally {
            try {
                if(fis!=null)
                    fis.close();
            } catch (IOException e) {
                LOG.debug("IO Close error",e);
            }
        }
    }
    /**
     * 采购订单明细
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param path
     */
    public void transPurchaseOrderCvs(String ip, int port, String username, String password, String path) {
        InputStream fis = null;
        try {
            transPurchaseOrderCvs();
            fis = new FileInputStream(new File(SAPConstants.PURCHASEORDER_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.PURCHASEORDER_FILE_NAME, fis);
        } catch (FileNotFoundException e) {
            LOG.error("ftp PurchaseOrder csv file notfound", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                LOG.debug("IO Close error", e);
            }
        }
    }

    /**
     * 采购订单（采购和退货）
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param path
     */
    public void transPurchaseReturnSalesCvs(String ip, int port, String username, String password, String path) {
        InputStream fis = null;
        try {
            transPurchaseReturnSalesCvs();
            fis = new FileInputStream(new File(SAPConstants.PURCHASERETURNSALES_FILE_PATH));
            FTPUtils.uploadFile(ip, port, username, password, path, SAPConstants.PURCHASERETURNSALES_FILE_NAME, fis);
        } catch (FileNotFoundException e) {
            LOG.error("ftp PurchaseOrder csv file notfound", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                LOG.debug("IO Close error", e);
            }
        }
    }

    /**
     * 首付款或全额（购买退货）流水
     */
    private void generatePaymentOrFullPaymentCsv() {
        List<String> orderStatusList = new ArrayList<>();
        orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());

        List<TxnOrderInfo> txnList = txnInfoService.selectByOrderStatusList(orderStatusList, getDateBegin(), getDateEnd());

        try {
            CsvWriter csvWriter = new CsvWriter(SAPConstants.PAYMENTORFULLPAYMENT_FILE_PATH, ',', Charset.forName("gbk"));
            //第一行空着
            csvWriter.writeRecord(new String[]{""});
            //表头
            String[] headers = {"GUID", "ZYWH", "ZTYPE", "ZSTATUS", "ERDAT", "ERZET", "ITEM", "WRBTR", "ZZHH", "ZZHH_COMP",
                    "ZZHH_NO", "ZDZ_LSH", "ZKK_LSH", "ZSF_LSH", "ZSFTD"};
            csvWriter.writeRecord(headers);
            int rowNum = 1;//行号
            for (TxnOrderInfo txn : txnList) {
                if (txn.getTxnType().equals(TxnTypeCode.XYZF_CODE.getCode())) {
                    continue;
                }
                List<String> contentList = new ArrayList<String>();
                contentList.add(txn.getTxnId().toString());
                contentList.add("");
                contentList.add("收款");
                contentList.add("S".equals(txn.getStatus()) ? "成功" : "失败");
                contentList.add(DateFormatUtil.dateToString(txn.getCreateDate(), DateFormatUtil.YYYY_MM_DD));
                contentList.add(DateFormatUtil.dateToString(txn.getCreateDate(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
                contentList.add(String.valueOf(rowNum));
                contentList.add(txn.getTxnAmt().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                if (txn.getTxnType().equals(TxnTypeCode.SF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())) {
                    //银联
                    contentList.add("");
                    contentList.add("6008");
                    contentList.add("97990155300001887");
                } else if (txn.getTxnType().equals(TxnTypeCode.ALIPAY_SF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())) {
                    //支付宝
                    contentList.add("cm2017082910000147");
                    contentList.add("6008");
                    contentList.add("97990155300001887");
                }
                contentList.add(txn.getMainOrderId());
                contentList.add(txn.getMainOrderId());


                if (txn.getTxnType().equals(TxnTypeCode.SF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())) {
                    //银联
                    contentList.add(txn.getOrigTxnId());
                    contentList.add("银联");

                } else if (txn.getTxnType().equals(TxnTypeCode.ALIPAY_SF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())) {
                    //支付宝
                    ApassTxnAttr apassTxnAttr = txnInfoService.getApassTxnAttrByTxnId(txn.getTxnId());
                    contentList.add(apassTxnAttr.getTxnId());
                    contentList.add("支付宝");
                }
                csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
                rowNum = rowNum + 1;
            }
            csvWriter.close();

        } catch (Exception e) {
            LOG.error("generatePaymentOrFullPaymentCsv error...", e);
        }
    }

    /**
     * 销售订单明细
     */
    private void generateSalesOrderInfoCsv() {
        List<String> orderStatusList = new ArrayList<>();
        orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());

        List<SalesOrderInfo> salOrderList = orderService.selectByOrderStatusList(orderStatusList, getDateBegin(), getDateEnd());

        try {
            CsvWriter csvWriter = new CsvWriter(SAPConstants.SALESORDERINFO_FILE_PATH, ',', Charset.forName("gbk"));
            //第一行空着
            csvWriter.writeRecord(new String[]{""});
            //表头
            String[] headers = {"GUID", "ZP_GUID", "ZLSH_M", "MATNR", "MAKTX", "ZSPGG", "NETPR", "BSTME", "KWMENG"};
            csvWriter.writeRecord(headers);
            int rowNum = 1;//行号
            for (SalesOrderInfo salOrder : salOrderList) {

                List<String> contentList = new ArrayList<String>();
                contentList.add(salOrder.getOrderdetailId().toString());
                contentList.add(salOrder.getOrderPrimayId().toString());
                contentList.add(String.valueOf(rowNum));
                contentList.add(salOrder.getGoodsCode());
                contentList.add(salOrder.getGoodsName());
                contentList.add(salOrder.getGoodsModel());
                contentList.add(salOrder.getGoodsPrice().setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                contentList.add(salOrder.getGoodsSkuAttr());
                contentList.add(salOrder.getGoodNum().toString());

                csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
                rowNum = rowNum + 1;
            }
            csvWriter.close();

        } catch (Exception e) {
            LOG.error("generateSalesOrderInfoCsv error...", e);
        }
    }

    /**
     * 销售订单(通过,退货)
     */
    private void generateSalesOrderCsv() {
        List<String> orderStatusList = new ArrayList<>();
        orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());

        List<SalesOrderPassOrRefund> salOrderList = orderService.selectSalesOrderStatusList(orderStatusList, getDateBegin(), getDateEnd());

        try {
            CsvWriter csvWriter = new CsvWriter(SAPConstants.SALESORDER_FILE_PATH, ',', Charset.forName("gbk"));
            //第一行空着
            csvWriter.writeRecord(new String[]{""});
            //表头
            String[] headers = {"GUID", "VKORG", "ZDDH", "AUART", "ZTEL_NO", "ZDDZT", "ZLSH_YDD", "ZZK", "ERDAT", "ERZET", "UNAME"};
            csvWriter.writeRecord(headers);
            for (SalesOrderPassOrRefund salOrder : salOrderList) {

                List<String> contentList = new ArrayList<String>();
                contentList.add(salOrder.getOrderPrimayId().toString());
                contentList.add("6008");
                contentList.add(salOrder.getOrderId());
                if(StringUtils.isNotBlank(salOrder.getRefundOrderId()) && StringUtils.equals(salOrder.getOrderId(),salOrder.getRefundOrderId())){
                    contentList.add("Y002");
                }else{
                    contentList.add("Y001");
                }
                contentList.add(salOrder.getTelephone());
                contentList.add("通过");
                contentList.add(salOrder.getOrderId());
                contentList.add(salOrder.getTotalDiscountAmount().setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                contentList.add(DateFormatUtil.dateToString(salOrder.getCreateDate(),DateFormatUtil.YYYY_MM_DD));
                contentList.add(DateFormatUtil.dateToString(salOrder.getCreateDate(),DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
                contentList.add(salOrder.getName());
                csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
            }
            csvWriter.close();

        } catch (Exception e) {
            LOG.error("generateSalesOrderCsv error...", e);
        }

    }

    private void generateCaiWuPingZhengCsv() throws Exception {
        List<String> orderStatusList = new ArrayList<>();
        orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());

        List<TxnOrderInfo> txnList = txnInfoService.selectByOrderStatusList(orderStatusList, getDateBegin(), getDateEnd());
        try {
            CsvWriter csvWriter = new CsvWriter(SAPConstants.CAIWUPINGZHENG_FILE_PATH, ',', Charset.forName("gbk"));
            //第一行空着
            csvWriter.writeRecord(new String[]{""});
            //表头
            String[] headers = {"GUID", "ERDAT", "ZTZLX", "KUNNR", "ZSFQE", "ZSFKBZ", "ZPTMC", "ZPTBM", "ZPTLSH", "ZZHH", "ZZHH_COMP",
                    "ZZHH_NO", "ZPTFWF", "ZDFF"};
            csvWriter.writeRecord(headers);
            for (TxnOrderInfo txn : txnList) {
                if (txn.getTxnType().equals(TxnTypeCode.XYZF_CODE.getCode())) {
                    continue;
                }
                List<String> contentList = new ArrayList<String>();
                contentList.add(txn.getTxnId() + "");
                contentList.add(DateFormatUtil.dateToString(txn.getPayTime(), DateFormatUtil.YYYY_MM_DD));
                contentList.add("2");
                contentList.add("3");
                if (txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())) {

                    contentList.add("Y");
                } else {
                    contentList.add("N");
                }
                contentList.add("1");
                contentList.add(ZPTMC);
                contentList.add(SAPConstants.PLATFORM_CODE);
                contentList.add(txn.getMainOrderId());
                if (txn.getTxnType().equals(TxnTypeCode.SF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())) {
                    //银联
                    contentList.add("");
                    contentList.add("6008");
                    contentList.add("97990155300001887");
                } else if (txn.getTxnType().equals(TxnTypeCode.ALIPAY_SF_CODE.getCode())
                        || txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())) {
                    //支付宝
                    contentList.add("cm2017082910000147");
                    contentList.add("6008");
                    contentList.add("97990155300001887");
                }
                contentList.add("6008");
                csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
            }

            csvWriter.close();

        } catch (Exception e) {
            LOG.error("generateCaiWuPingZhengCsv error...", e);
        }
    }

    private void transPurchaseReturnSalesCvs() {
        CsvWriter csvWriter = null;
        List<String> orderList = new ArrayList<>();
        orderList.add(OrderStatus.ORDER_COMPLETED.getCode());
        List<String> returnStatusList = new ArrayList<>();
        returnStatusList.add(RefundStatus.REFUND_STATUS05.getCode());
        List<String> returnTypeList = new ArrayList<>();
        returnTypeList.add("0");
        List<PurchaseReturnOrder> txnList = txnInfoService.selectPurchaseReturnSalesList(orderList,returnStatusList,returnTypeList,getDateBegin(),getDateEnd());
        try {
            csvWriter = new CsvWriter(SAPConstants.PAYMENTORFULLPAYMENT_FILE_PATH, ',', Charset.forName("gbk"));
            //第一行空
            csvWriter.writeRecord(new String[]{""});
            //表头
            String[] headers = {"GUID", "BUKRS", "ZDDH_XMZ", "BSART", "LIFNR", "NAME1", "VERTN", "ZYF", "ZLSH_YDD","ERDAT", "ERZET"};
            csvWriter.writeRecord(headers);
            for (PurchaseReturnOrder txn : txnList) {
                List<String> contentList = new ArrayList<String>();
                contentList.add(txn.getOrderInfoId().toString());
                contentList.add(txn.getCompanyCode());
                contentList.add(txn.getOrderId());
                contentList.add(txn.getOrderType());
                String suqNo = txn.getSupNo();
                String merchantCode = txn.getMerchantCode();
                MerchantCode[] codeArr = MerchantCode.values();
                for(MerchantCode entity : codeArr){
                    if(StringUtils.equals(merchantCode, entity.getCode())){
                        suqNo = entity.getName();
                        break;
                    }
                }
                contentList.add(merchantCode);
                contentList.add(suqNo);
                contentList.add(txn.getCarriage());
                contentList.add(txn.getOldOrderId());
                contentList.add(DateFormatUtil.dateToString(txn.getCreateDate(), DateFormatUtil.YYYY_MM_DD));
                contentList.add(DateFormatUtil.dateToString(txn.getCreateDate(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
                csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
            }
        } catch (Exception e) {
            LOG.error("PurchaseReturnSalesCvs error...", e);
        } finally {
            if (csvWriter != null)
                csvWriter.close();
        }
    }

    private void transPurchaseOrderCvs() {
        CsvWriter csvWriter = null;
        List<String> orderStatus = new ArrayList<String>();
        orderStatus.add(OrderStatus.ORDER_COMPLETED.getCode());
        List<PurchaseOrderDetail> txnList = txnInfoService.selectPurchaseOrderList(orderStatus,getDateBegin(),getDateEnd());
        try{
            csvWriter = new CsvWriter(SAPConstants.VBSBUSINESS_FILE_PATH,',', Charset.forName("UTF-8"));
            //第一列空
            csvWriter.writeRecord(new String[]{});
            //必选表头
            String[] headers = {"GUID","P_GUID","ZLSH_M","MATNR","MAKTX","NETPR","BSTME","KWMENG"};
            csvWriter.writeRecord(headers);
            int i = 1;
            for(Iterator<PurchaseOrderDetail> it = txnList.iterator(); it.hasNext();){
                PurchaseOrderDetail entity = it.next();
                List<String> contentList = new ArrayList<String>();
                contentList.add(entity.getOrderInfoId().toString());
                contentList.add(entity.getOrderId().toString());
                contentList.add(i+"");
                i++;
                contentList.add(entity.getGoodsCode());
                contentList.add(entity.getGoodsName());
                contentList.add(entity.getGoodsCostPrice().toString());
                contentList.add(entity.getAcceptGoodsType());
                contentList.add(entity.getStockCurrAmt().toString());
                csvWriter.writeRecord((String[]) contentList.toArray());
            }
        }catch (Exception e) {
            LOG.error("PurchaseReturnSalesCvs error...",e);
        }finally {
            if(csvWriter!=null)
                csvWriter.close();
        }
    }
	private void transVBSBusinessNumCvs(){
		CsvWriter csvWriter = null;
		List<String> orderStatusList = new ArrayList<>();
	    orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());
	    List<TxnOrderInfo> txnList = txnInfoService.selectVBSBusinessNumList(orderStatusList,getDateBegin(),getDateEnd());
	    try{
	    	csvWriter = new CsvWriter(SAPConstants.VBSBUSINESS_FILE_PATH,',', Charset.forName("UTF-8"));
	        //第一列空
	        csvWriter.writeRecord(new String[]{});
	        //必选表头
	        String[] headers = {"GUID","ZPTMC","ZPTBM","ZLSH_DD","ZYWH_VBS","ERDAT","ERZET"};
	        csvWriter.writeRecord(headers);
	        for(TxnOrderInfo txn : txnList){
	        	if(txn.getTxnType().equals(TxnTypeCode.XYZF_CODE.getCode())){
	        		continue;
	        	}
	        	List<String> contentList = new ArrayList<String>();
                /*GUID*/
                contentList.add(txn.getTxnId() + "'");
	        	/*ZPTMC*/
                contentList.add(ZPTMC);
	        	/*ZPTBM*/
                contentList.add(SAPConstants.PLATFORM_CODE);
	            /*ZLSH_DD  子订单号*/
                contentList.add(txn.getOrderId());
	            /*ZYWH_VBS*/
	            contentList.add(txn.getLoanId().toString());
	            String createdDate = DateFormatUtil.dateToString(txn.getCreateDate(),DateFormatUtil.YYYY_MM_DD);
	            String createdtime = DateFormatUtil.dateToString(txn.getCreateDate(),DateFormatUtil.YYYY_MM_DD_HH_MM_SS);
	            /*ERDAT*/
                contentList.add(createdDate);
	            /*ERZET*/
                contentList.add(createdtime);
	            /*可选表头UNAME,ZSJLY*/
	            /*write*/
	            csvWriter.writeRecord((String[]) contentList.toArray());
	        }
	    }catch (Exception e){
	        LOG.error("VBSBusinessCvs error...",e);
	    }finally {
	    	if(csvWriter!=null)
	    		csvWriter.close();
		}
	}
	private String getDateBegin(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE,-1);
		return DateFormatUtil.dateToString(cal.getTime(),DateFormatUtil.YYYY_MM_DD);
	}
	private String getDateEnd(){
		return DateFormatUtil.dateToString(new Date(),DateFormatUtil.YYYY_MM_DD);
	}
	/**
	   * 财务凭证调整明细
	   */
	  private void generateCaiWuPingZhengCsv2() throws Exception{
	    List<String> orderStatusList = new ArrayList<>();
	    orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());

	    List<TxnOrderInfo> txnList = txnInfoService.selectByOrderStatusList(orderStatusList,getDateBegin(),getDateEnd());
	    try{
	      CsvWriter csvWriter = new CsvWriter(SAPConstants.CAIWUPINGZHENG_FILE_PATH2,',', Charset.forName("gbk"));
	      //第一行空着
	      csvWriter.writeRecord(new String[]{""});
	      //表头
	      String[] headers = {"GUID","P_GUID","ZPTLSH","ITEM","ZFYLX","WRBTR"};
	      csvWriter.writeRecord(headers);
	      int i = 0;
	      for(TxnOrderInfo txn : txnList){
	        ++i;
	        if(txn.getTxnType().equals(TxnTypeCode.XYZF_CODE.getCode())){
	          continue;
	        }
	        List<String> contentList = new ArrayList<String>();
	        contentList.add(txn.getExtOrderId()+"");
	        contentList.add(txn.getTxnId()+"");
	        contentList.add(txn.getMainOrderId());
	        contentList.add(i+"");
	        if(txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())
	            || txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())){

	          contentList.add("Z047");
	        }else{
	          contentList.add("Z044");
	        }
	          contentList.add(txn.getTxnAmt()+"");
	        csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
	      }

	      csvWriter.close();

	    }catch (Exception e){
	      LOG.error("generateCaiWuPingZhengCsv2 error...",e);
	    }
	}
	private void transPurchaseOrReturnCvs() {
        List<String> orderStatusList = new ArrayList<>();
        orderStatusList.add(OrderStatus.ORDER_COMPLETED.getCode());
        List<TxnOrderInfo> txnList = txnInfoService.selectByOrderStatusList(orderStatusList,getDateBegin(),getDateEnd());
        CsvWriter csvWriter = null;
        try {
            csvWriter = new CsvWriter(SAPConstants.PURCHASEORRETURN_FILE_PATH,',', Charset.forName("gbk"));
            //第一行空着
            csvWriter.writeRecord(new String[]{});
            //表头
            String[] headers = {"GUID","ZTYPE","ZSTATUS","ERDAT","ERZET","ITEM","WRBTR","ZZHH","ZZHH_COMP","ZZHH_NO","ZDZ_LSH","ZKK_LSH","ZSF_LSH","ZSFTD"};
            csvWriter.writeRecord(headers);
            Integer rowNum = new Integer("1");//行号
            for(TxnOrderInfo txn : txnList){
                if(txn.getTxnType().equals(TxnTypeCode.XYZF_CODE.getCode())){
                    continue;
                }
                List<String> contentList = new ArrayList<String>();
                contentList.add(txn.getTxnId().toString());
                contentList.add("收款");
                contentList.add("S".equals(txn.getStatus())?"收款成功":"收款失败");
                contentList.add(DateFormatUtil.dateToString(txn.getCreateDate(),DateFormatUtil.YYYY_MM_DD));
                contentList.add(DateFormatUtil.dateToString(txn.getCreateDate(),DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
                contentList.add(String.valueOf(rowNum));
                contentList.add(txn.getTxnAmt().setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                if(txn.getTxnType().equals(TxnTypeCode.SF_CODE.getCode())
                    || txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())){
                      //银联
                    contentList.add("");
                    contentList.add("6008");
                    contentList.add("97990155300001887");
                }else if(txn.getTxnType().equals(TxnTypeCode.ALIPAY_SF_CODE.getCode())
                    ||txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())){
                     //支付宝
                    contentList.add("cm2017082910000147");
                    contentList.add("6008");
                    contentList.add("97990155300001887");
                }
                contentList.add(txn.getMainOrderId());
                contentList.add(txn.getMainOrderId());
                if(txn.getTxnType().equals(TxnTypeCode.SF_CODE.getCode())
                    || txn.getTxnType().equals(TxnTypeCode.KQEZF_CODE.getCode())){
                    //银联
                    contentList.add(txn.getOrigTxnId());
                    contentList.add("银联");
                }else if(txn.getTxnType().equals(TxnTypeCode.ALIPAY_SF_CODE.getCode())
                    ||txn.getTxnType().equals(TxnTypeCode.ALIPAY_CODE.getCode())){
                    //支付宝
                    ApassTxnAttr apassTxnAttr = txnInfoService.getApassTxnAttrByTxnId(txn.getTxnId());
                    contentList.add(apassTxnAttr.getTxnId());
                    contentList.add("支付宝");
                }
                csvWriter.writeRecord(contentList.toArray(new String[contentList.size()]));
                rowNum ++;
            }
        }catch (Exception e){
            LOG.error("generatePaymentOrFullPaymentCsv error...",e);
        }finally {
            if(csvWriter!=null)
                csvWriter.close();
        }
	}
}