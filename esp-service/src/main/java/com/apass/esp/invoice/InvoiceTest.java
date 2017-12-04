package com.apass.esp.invoice;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.aisino.EncryptionDecryption;
import com.aisino.FarmartJavaBean;
import com.apass.esp.domain.entity.order.OrderInfoEntity;
import com.apass.esp.invoice.model.FaPiaoKJ;
import com.apass.esp.invoice.model.FaPiaoKJDD;
import com.apass.esp.invoice.model.FaPiaoKJXM;
public class InvoiceTest {
    public static void main(String[] args) throws Exception {
        
////        InvoiceService invoiceService = new InvoiceService();
////        OrderInfoEntity order = new OrderInfoEntity();
////        order.setOrderId("60901406229");
////        Integer i = invoiceService.invoiceCheck(order);
//        
//        FaPiaoKJ faPiaoKJ = new FaPiaoKJ();
//        faPiaoKJ.setFpqqlsh("d2222222222222221234");
//        faPiaoKJ.setDsptbm("111MFWIK");
//        faPiaoKJ.setNsrsbh("310101000000090");
//        faPiaoKJ.setNsrmc("雅诗兰黛（上海）商贸有限公司");
//        faPiaoKJ.setNsrdzdah(StringUtils.EMPTY);
//        faPiaoKJ.setSwjgDm(StringUtils.EMPTY);
//        faPiaoKJ.setDkbz("0");
//        faPiaoKJ.setKpxm("化妆品");
//        faPiaoKJ.setBmbBbh("1.0");
//        faPiaoKJ.setXhfNsrsbh("310101000000090");
//        faPiaoKJ.setXhfmc("雅诗兰黛（上海）商贸有限公司");
//        faPiaoKJ.setXhfDz("上海市闵行区金都路3688号301、302、306室");
//        faPiaoKJ.setXhfDh("22039999");
//        faPiaoKJ.setGhfmc("许嘉心");
//        faPiaoKJ.setGhfqylx("01");
//        faPiaoKJ.setKpy("财务");
//        faPiaoKJ.setKplx("1");
//        faPiaoKJ.setCzdm("10");
//        faPiaoKJ.setQdBz("0");
//        faPiaoKJ.setKphjje("20");
//        faPiaoKJ.setHjbhsje("0");
//        faPiaoKJ.setHjse("0");
////        faPiaoKJ.setPydm("");
////        faPiaoKJ.setXhfYhzh("");
////        faPiaoKJ.setGhfNsrsbh("");
////        faPiaoKJ.setGhfSf("");
////        faPiaoKJ.setGhfDz("");
////        faPiaoKJ.setGhfDz("");
//        faPiaoKJ.setGhfSj("");
////        faPiaoKJ.setGhfEmail("");
////        faPiaoKJ.setGhfYhzh("");
////        faPiaoKJ.setHyDm("");
////        faPiaoKJ.setHyMc("");
////        faPiaoKJ.setSky("");
////        faPiaoKJ.setFhr("");
////        faPiaoKJ.setKprq("");
////        faPiaoKJ.setYfpDm("");
////        faPiaoKJ.setYfpHm("");
////        faPiaoKJ.setQdxmmc("");
////        faPiaoKJ.setChyy("");
////        faPiaoKJ.setTschbz("");
////        faPiaoKJ.setBz("");
////        faPiaoKJ.setByzd1("");
////        faPiaoKJ.setByzd2("");
////        faPiaoKJ.setByzd3("");
////        faPiaoKJ.setByzd4("");
////        faPiaoKJ.setByzd5("");
//        faPiaoKJ = (FaPiaoKJ) FarmartJavaBean.farmartJavaB(faPiaoKJ, FaPiaoKJ.class);
//
//        List<FaPiaoKJXM> list = new ArrayList<FaPiaoKJXM>();
//        FaPiaoKJXM faPiaoKJXM = new FaPiaoKJXM();
//        faPiaoKJXM.setXmmc("0J3M01 净痘凝胶 10ML ");
//        faPiaoKJXM.setXmsl("1");
//        faPiaoKJXM.setHsbz("1");
//        faPiaoKJXM.setFphxz("0");
//        faPiaoKJXM.setXmdj("20.0");
//        faPiaoKJXM.setSpbm("1010101030000000000");
//        faPiaoKJXM.setYhzcbs("0");
//        faPiaoKJXM.setLslbs("");
//        faPiaoKJXM.setZzstsgl("");
//        faPiaoKJXM.setKce("0");
//        faPiaoKJXM.setXmje("20.0");
//        faPiaoKJXM.setSl("0.17");
//        faPiaoKJXM = (FaPiaoKJXM) FarmartJavaBean.farmartJavaB(faPiaoKJXM, FaPiaoKJXM.class);
////        faPiaoKJXM.setXmdw("");
////        faPiaoKJXM.setGgxh("");
////        faPiaoKJXM.setZxbm("");
////        faPiaoKJXM.setSe("");
////        faPiaoKJXM.setByzd1("");
////        faPiaoKJXM.setByzd2("");
////        faPiaoKJXM.setByzd3("");
////        faPiaoKJXM.setByzd4("");
////        faPiaoKJXM.setByzd5("");
//        list.add(faPiaoKJXM);
//
//        FaPiaoKJDD faPiaoKJDD = new FaPiaoKJDD();
//        faPiaoKJDD.setDdh("2492684718573093");
//        faPiaoKJDD.setThdh("2492684718573093");
//        faPiaoKJDD.setDddate("2016-10-31 10:47:17");
//        InvoiceIssueService service = new InvoiceIssueService();
//        String s = service.requestFaPiaoKJ(faPiaoKJ,list,faPiaoKJDD);
//        System.out.println(s);
//        System.out.println(EncryptionDecryption.getFaPiaoReturnState(s).getReturnMessage());
//
//        FaPiaoDLoad faPiaoDLoad = new FaPiaoDLoad();
//        faPiaoDLoad.setDdh("2492684718573093");
//        faPiaoDLoad.setDsptbm("111MFWIK");
//        faPiaoDLoad.setFpqqlsh("d2222222222222221234");
//        faPiaoDLoad.setNsrsbh("310101000000090");
//        faPiaoDLoad.setPdfXzfs("3");
//        String sS = service.requestFaPiaoDL(faPiaoDLoad);
//        System.out.println(sS);
////        System.out.println(EncryptionDecryption.getFaPiaoReturnState(sS).getReturnMessage());
//        System.out.println(EncryptionDecryption.getRequestFaPiaoDL(sS));
//        System.out.println(EncryptionDecryption.getRequestFaPiaoDLRetuen(sS));
    }
}