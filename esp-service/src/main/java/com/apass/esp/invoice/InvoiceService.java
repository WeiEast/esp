package com.apass.esp.invoice;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.dto.InvoiceDto;
import com.apass.esp.domain.entity.Invoice;
import com.apass.esp.domain.entity.invoice.InvoiceDetails;
import com.apass.esp.mapper.InvoiceMapper;
import com.apass.gfb.framework.utils.DateFormatUtil;
/**
 * 电子发票
 * @author Administrator
 *
 */
@Service
public class InvoiceService {
    @Autowired
    private InvoiceMapper invoiceMapper;
    @Autowired
    private InvoiceIssueService invoiceIssueService;
    /**
     * CREATED
     * @param entity
     * @return
     */
    @Transactional
    public Long createdEntity(Invoice entity) {
        Integer i = invoiceMapper.insertSelective(entity);
        if(i==1){
            return entity.getId();
        }
        return null;
    }
    /**
     * READ BY ID
     * @param id
     * @return
     */
    public Invoice readEntity(Long id) {
        return invoiceMapper.selectByPrimaryKey(id);
    }
    /**
     * READ LIST
     * @param userId
     * @return
     */
    public List<Invoice> readEntityList(Long userId) {
        Invoice entity = new Invoice();
        entity.setUserId(userId);
        return invoiceMapper.getInvoiceList(entity);
    }
    /**
     * READ LIST
     * @param entity
     * @return
     */
    public List<Invoice> readEntityList(Invoice entity) {
        return invoiceMapper.getInvoiceList(entity);
    }
    /**
     * UPDATED
     * @param entity
     * @return
     */
    @Transactional
    public Invoice updatedEntity(Invoice entity) {
        Integer i = invoiceMapper.updateByPrimaryKeySelective(entity);
        if(i==1){
            return entity;
        }
        return null;
    }
    /**
     * DELETE BY ID
     * @param entity
     * @return
     */
    @Transactional
    public Integer deleteEntity(Invoice entity) {
        return invoiceMapper.deleteByPrimaryKey(entity.getId());
    }
    /**
     * DELETE BY ID
     * @param entity
     * @return
     */
    @Transactional
    public Integer deleteEntity(Long id) {
        return invoiceMapper.deleteByPrimaryKey(id);
    }
    /**
     * 根据订单查询发票 以及发票状态
     * @param userId
     * @param orderId
     * @return
     */
    public Response invoiceDetails(Long userId, String orderId) {
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setOrderId(orderId);
        List<Invoice> list = readEntityList(invoice);
        InvoiceDetails de = new InvoiceDetails();
        if(list!=null&&list.size()>0){
            invoice = list.get(0);
            BeanUtils.copyProperties(invoice, de);
            de.setInvoiceType("电子增值税普通发票");
            de.setAParty("上海奥派数据科技有限公司");
            de.setOrderAmt(invoice.getOrderAmt());
            de.setInvoiceHead(invoice.getHeadType()==new Byte("1")?"个人发票":invoice.getCompanyName());
            de.setDate(DateFormatUtil.datetime2String(invoice.getCreatedTime()));
            Byte status = invoice.getStatus();
            if(status==new Byte("2")){
                de.setStatus("申请成功");
            }else{
                de.setStatus("申请中");
            }
        }
        return Response.success("发票详情查询成功", de);
    }
    /**
     * 查询用户开票记录
     * @param userId
     * @return
     */
    public Response invoiceRecord(Long userId) {
        Invoice entity = new Invoice();
        entity.setUserId(userId);
        List<Invoice> list = readEntityList(entity);
        List<InvoiceDetails> detailsApply = new ArrayList<InvoiceDetails>();
        for(Invoice invoice : list){
            InvoiceDetails en = new InvoiceDetails();
            BeanUtils.copyProperties(invoice, en);
            en.setDate(DateFormatUtil.getCurrentTime(null));
            en.setInvoiceType("电子增值税普通发票");
            en.setInvoiceHead(entity.getHeadType()==new Byte("1")?"个人发票":entity.getCompanyName());
            en.setAParty("上海奥派数据科技有限公司");
            en.setOrderAmt(invoice.getOrderAmt());
            en.setDate(DateFormatUtil.datetime2String(invoice.getCreatedTime()));
            if(StringUtils.isBlank(invoice.getInvoiceNum())){
                en.setStatus("申请中");
                en.setInvoiceNum("暂无");
            }else{
                en.setStatus("开票成功");
                en.setInvoiceNum(entity.getInvoiceNum());
            }
            detailsApply.add(en);
        }
        return Response.success("开票记录查询成功", detailsApply);
    }
    /**
     *创建发票
     */
    @Transactional(rollbackFor = Exception.class)
    public Invoice createInvoice(InvoiceDto invoiceDto){
        Invoice in = new Invoice();
        in.setCompanyName(invoiceDto.getCompanyName());
        in.setContent(invoiceDto.getContent());
        in.setCreatedTime(new Date());
        in.setOrderAmt(invoiceDto.getOrderAmt());
        in.setTelphone(invoiceDto.getTelphone());
        in.setUserId(invoiceDto.getUserId());
        in.setUpdatedTime(new Date());
        in.setStatus((byte)1);
        in.setHeadType((byte)1);
        in.setTaxpayerNum(invoiceDto.getTaxpayerNum());
        in.setOrderId(invoiceDto.getOrderId());
        in.setSeller("上海奥派数据科技有限公司");
        invoiceMapper.insertSelective(in);
        return in;
    }
}