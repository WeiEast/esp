package com.apass.esp.web.order;

import com.apass.esp.domain.dto.aftersale.CashRefundDto;
import com.apass.esp.domain.dto.aftersale.CashRefundDtoVo;
import com.apass.esp.domain.dto.order.OrderDetailInfoDto;
import com.apass.esp.service.order.OrderService;
import com.apass.esp.service.refund.CashRefundService;
import com.apass.esp.utils.BeanUtils;
import com.apass.esp.utils.ResponsePageBody;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.gfb.framework.utils.HttpWebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * type: class
 *
 * @author xianzhi.wang
 * @see
 * @since JDK 1.8
 */
@Controller
@RequestMapping("/application/business/cashRefund")
public class AfsQueryController {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(AfsQueryController.class);

    @Autowired
    private CashRefundService cashRefundService;

    @Autowired
    private OrderService orderService;

    @ResponseBody
    @RequestMapping("/getCashRefundByOrderId")
    public ResponsePageBody<CashRefundDtoVo> getCashRefundByOrderId(HttpServletRequest request) {
        ResponsePageBody<CashRefundDtoVo> responsePageBody = new ResponsePageBody();
        List<CashRefundDtoVo> list = new ArrayList();

        String orderId = HttpWebUtils.getValue(request, "orderId");
        CashRefundDto cashRefundDto = cashRefundService.getCashRefundByOrderId(orderId);
        if(cashRefundDto != null) {
            CashRefundDtoVo cashRefundDtoVo = new CashRefundDtoVo();
            BeanUtils.copyProperties(cashRefundDtoVo, cashRefundDto);
            cashRefundDtoVo.setCreateDateStr(DateFormatUtil.dateToString(cashRefundDtoVo.getCreateDate(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
            try {
                OrderDetailInfoDto orderDetailInfoDto = orderService.getOrderDetailInfoDto("", orderId);
                cashRefundDtoVo.setTotalNum(orderDetailInfoDto.getGoodsNumSum());
                list.add(cashRefundDtoVo);

                responsePageBody.setMsg("返回成功");
                responsePageBody.setStatus("1");
                responsePageBody.setRows(list);
            } catch (Exception e) {
                responsePageBody.setMsg("返回失败");
                responsePageBody.setStatus("0");
                responsePageBody.setRows(list);
            }
        }

        return responsePageBody;
    }

}
