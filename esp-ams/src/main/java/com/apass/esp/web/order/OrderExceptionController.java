package com.apass.esp.web.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.apass.esp.domain.Response;
import com.apass.esp.domain.entity.bill.TxnInfoEntity;
import com.apass.esp.domain.entity.merchant.MerchantInfoEntity;
import com.apass.esp.domain.entity.order.OrderDetailInfoEntity;
import com.apass.esp.domain.entity.order.OrderInfoEntity;
import com.apass.esp.domain.entity.order.OrderSubInfoEntity;
import com.apass.esp.domain.enums.TxnTypeCode;
import com.apass.esp.domain.vo.FydActivity;
import com.apass.esp.mapper.TxnInfoMapper;
import com.apass.esp.repository.merchant.MerchantInforRepository;
import com.apass.esp.schedule.DataAppuserAnalysisSchedule;
import com.apass.esp.schedule.DataAppuserRetentionSchedule;
import com.apass.esp.schedule.MailStatis2ScheduleTask;
import com.apass.esp.schedule.OrderChannelStatisticsScheduleTask;
import com.apass.esp.schedule.OrderInforMailSendScheduleTask;
import com.apass.esp.schedule.OrderModifyStatusScheduleTask;
import com.apass.esp.schedule.OrderTransactionDetailSchedeuleTask;
import com.apass.esp.schedule.TalkingDataScheduleTask;
import com.apass.esp.service.offer.MyCouponManagerService;
import com.apass.esp.service.order.OrderService;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.mybatis.page.Page;
import com.apass.gfb.framework.mybatis.page.Pagination;
import com.apass.gfb.framework.security.toolkit.SpringSecurityUtils;
import com.apass.gfb.framework.utils.BaseConstants.CommonCode;
import com.apass.gfb.framework.utils.HttpWebUtils;
import com.google.common.collect.Maps;

/**
 * 异常订单处理类，此类主要用作，支付宝二次退款的处理
 * 
 */
@Controller
@RequestMapping("/application/business/order/exception")
public class OrderExceptionController {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(OrderExceptionController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private TxnInfoMapper txnInfoMapper;

    @Autowired
    private OrderInforMailSendScheduleTask task;
    
    @Autowired
    private OrderTransactionDetailSchedeuleTask detailTask;
    
    @Autowired
    private OrderChannelStatisticsScheduleTask channelTask;

    @Autowired
    private MerchantInforRepository merchant;

    @Autowired
    private TalkingDataScheduleTask talkingDataScheduleTask;

    @Autowired
    private OrderModifyStatusScheduleTask task1;
    
    @Autowired
    private MailStatis2ScheduleTask task2;
    
    @Autowired
    private DataAppuserAnalysisSchedule analysisSchedule;
    
    @Autowired
    private DataAppuserRetentionSchedule retentionSchedule;
    
    @Autowired
    private MyCouponManagerService managerService;
    /**
     * 订单信息页面
     */
    @RequestMapping("/page")
    public ModelAndView handlePage(Map<String, Object> paramMap) {
        return new ModelAndView("order/order-exception", paramMap);
    }

    /**
     * 订单信息查询
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/pagelist")
    public ResponsePageBody<OrderSubInfoEntity> handlePageList(HttpServletRequest request) {
        ResponsePageBody<OrderSubInfoEntity> respBody = new ResponsePageBody<OrderSubInfoEntity>();
        try {
            // 分页参数
            Page page = getPageParam(request);

            String refundType = HttpWebUtils.getValue(request, "refundType");

            Map<String, String> map = Maps.newHashMap();

            Pagination<OrderSubInfoEntity> orderList = new Pagination<OrderSubInfoEntity>();

            if (StringUtils.equals(refundType, "1")) {
                orderList = orderService.queryOrderRefundException(map, page);
            } else {
                orderList = orderService.queryOrderCashRefundException(map, page);
            }

            List<OrderSubInfoEntity> dataList = orderList.getDataList();

            respBody.setTotal(orderList.getTotalCount());
            respBody.setRows(dataList);
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            LOG.error("异常订单列表查询失败", e);
            respBody.setMsg("异常订单列表查询失败");
        }
        return respBody;
    }

    private Page getPageParam(HttpServletRequest request) {
        String pageNo = HttpWebUtils.getValue(request, "page");
        String pageSize = HttpWebUtils.getValue(request, "rows");
        Page page = new Page();
        if (pageNo != null && pageSize != null) {
            Integer pageNoNum = Integer.parseInt(pageNo);
            Integer pageSizeNum = Integer.parseInt(pageSize);
            page.setPage(pageNoNum <= 0 ? 1 : pageNoNum);
            page.setLimit(pageSizeNum <= 0 ? 1 : pageSizeNum);
        }

        return page;
    }

    /**
     * 更新退款状态
     */
    @ResponseBody
    @RequestMapping("/refund")
    public Response refundCash(String orderId, String refundType) {
        try {
            // 获取请求的参数
            if (StringUtils.isBlank(orderId)) {
                throw new BusinessException("订单号不能为空!");
            }
            orderService.orderCashRefund(orderId, refundType, SpringSecurityUtils.getLoginUserDetails()
                    .getUsername());
        } catch (BusinessException e) {
            LOG.error("退款操作失败", e);
            return Response.fail(e.getErrorDesc());
        }
        return Response.success("退款操作成功！");
    }

    /**
     * 订单详情信息查询
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryOrderDetailInfo")
    public ResponsePageBody<OrderDetailInfoEntity> queryOrderDetailInfo(HttpServletRequest request) {
        ResponsePageBody<OrderDetailInfoEntity> respBody = new ResponsePageBody<OrderDetailInfoEntity>();
        try {
            // 获取请求的参数
            String orderId = HttpWebUtils.getValue(request, "orderId");
            // 通过商户号查收订单详情信息
            List<OrderDetailInfoEntity> orderDetailList = orderService.queryOrderDetailInfo(null, orderId);
            for (OrderDetailInfoEntity detail : orderDetailList) {
                detail.setGoodsAmt(detail.getGoodsPrice().multiply(new BigDecimal(detail.getGoodsNum())));
                // 首先根据订单的Id，查询订单的信息，然后根据订单的main_order_id ,查询交易流水表
                BigDecimal refundAmt = detail.getGoodsAmt().multiply(getScale(orderId));
                detail.setRefundAmt(refundAmt);
                MerchantInfoEntity entity = merchant.queryByMerchantCode(detail.getMerchantCode());
                detail.setMerchantCode(null != entity ? entity.getMerchantName() : detail.getMerchantCode());
            }
            respBody.setTotal(orderDetailList.size());
            respBody.setRows(orderDetailList);
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            LOG.error("用户列表查询失败", e);
            respBody.setMsg("用户列表查询失败");
        }
        return respBody;
    }

    /**
     * 计算退款比例
     * 
     * @param orderId
     * @return
     * @throws BusinessException
     */
    public BigDecimal getScale(String orderId) throws BusinessException {

        OrderInfoEntity order = orderService.selectByOrderId(orderId);

        List<TxnInfoEntity> txnInfoEntityList = txnInfoMapper.selectByOrderId(order.getMainOrderId());
        BigDecimal txtAmount = new BigDecimal(0);
        BigDecimal firstAmount = new BigDecimal(0);
        for (TxnInfoEntity txnInfoEntity : txnInfoEntityList) {
            txtAmount = txtAmount.add(txnInfoEntity.getTxnAmt());
            if (txnInfoEntity.getTxnType().equalsIgnoreCase(TxnTypeCode.ALIPAY_SF_CODE.getCode())) {
                firstAmount = txnInfoEntity.getTxnAmt();
            }
            if (StringUtils.equals(TxnTypeCode.ALIPAY_CODE.getCode(), txnInfoEntity.getTxnType())) {
                firstAmount = txnInfoEntity.getTxnAmt();
            }
        }
        return firstAmount.divide(txtAmount);
    }

    @ResponseBody
    @RequestMapping("/daily")
    public Response sendOrderMailEveryDay() {
        task.sendOrderMailEveryDay();
        return Response.success("发送成功");
    }

    @ResponseBody
    @RequestMapping("/monthly")
    public Response sendOrderMailOn1stMonth() {
        task.sendOrderMailOn1stMonth();
        return Response.success("发送成功");
    }
    
    @ResponseBody
    @RequestMapping("/detailTask")
    public Response detailTask(){
    	detailTask.sendOrderMailEveryDay();
    	return Response.success("发送成功");
    }
    
    @ResponseBody
    @RequestMapping("/channelTask")
    public Response channelTask(){
    	channelTask.channelStatistics();
    	return Response.success("发送成功");
    }
    @ResponseBody
    @RequestMapping("/talkingDataTask1")
    public Response talkingDataTask1(){
        talkingDataScheduleTask.schedule();
    	return Response.success("发送成功");
    }
    @ResponseBody
    @RequestMapping("/talkingDataTask2")
    public Response talkingDataTask2(){
        try {
            talkingDataScheduleTask.schedule2();
        } catch (InterruptedException e) {
            LOG.error("---------------Exception:--------->",e);
        }
        return Response.success("发送成功");
    }

    @ResponseBody
    @RequestMapping("/time")
    public Response scheduledDelivery(){
    	task1.updateOrderStatusAndPreDelivery();
    	return Response.success("定时发货成功!");
    }
    
    @ResponseBody
    @RequestMapping("/mail/statis")
    public Response mailStatisSchedule(){
    	task2.mailStatisSchedule();
    	return Response.success("安家趣花电商订单统计(成交金额，统计成本)!");
    }
    
    @ResponseBody
    @RequestMapping("/every/hour")
    public Response everyHoursSchedule(){
    	analysisSchedule.everyHoursSchedule();
    	return Response.success("每小时插入更新数据成功(t_data_appuser_analysis)!");
    }
    
    @ResponseBody
    @RequestMapping("/updateAnalysisRegisterUser")
    public Response updateAnalysisRegisterUser(){
    	analysisSchedule.updateAnalysisRegisterUser();
    	return Response.success("更新注册用户数据成功(t_data_appuser_analysis)!");
    }
    
    @ResponseBody
    @RequestMapping("/analysis")
    public Response everyDayScheduleData(){
    	analysisSchedule.everyDayScheduleData();
    	return Response.success("每天插入更新数据成功(t_data_appuser_analysis)!");
    }
    
    @ResponseBody
    @RequestMapping("/retention")
    public Response retentionEveryDayScheduleData(){
    	retentionSchedule.retentionEveryDayScheduleData();
    	return Response.success("每天插入更新数据成功(t_data_appuser_retention)!");
    }
    
    @ResponseBody
    @RequestMapping("/message")
    public Response sendMessage() throws BusinessException{
    	FydActivity fyd = new FydActivity();
    	fyd.setMobile("17621964882");
    	fyd.setScene("IDENTITY");
    	managerService.addFYDYHZY(fyd);
    	return Response.success("房易贷发送优惠券短信息成功!");
    }
}
