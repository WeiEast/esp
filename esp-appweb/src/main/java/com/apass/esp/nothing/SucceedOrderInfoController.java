package com.apass.esp.nothing;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.apass.esp.common.code.BusinessErrorCode;
import com.apass.esp.domain.Response;
import com.apass.esp.service.order.OrderService;
import com.apass.gfb.framework.utils.BaseConstants.ParamsCode;
import com.apass.gfb.framework.utils.CommonUtils;
import com.google.common.collect.Maps;

@Path("/order")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SucceedOrderInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SucceedOrderInfoController.class);
    @Autowired
    public OrderService orderService;

    private static final String NO_USER = "对不起!用户号不能为空";

    @POST
    @Path("/latest/ordertime")
    public Response latestOrderTime(Map<String, Object> paramMap) {
        try {
            String userIdStr = CommonUtils.getValue(paramMap, ParamsCode.USER_ID);
            if (null == userIdStr) {
                return Response.fail(NO_USER);
            }
            if (!StringUtils.isNumeric(userIdStr)) {
            	LOGGER.error("用户名传入非法!");
                return Response.fail(BusinessErrorCode.PARAM_VALUE_ERROR);
            }
            Long userId = Long.valueOf(userIdStr);
            String orderDate = orderService.latestSuccessTime(userId);
            Map<String, String> resultMap = Maps.newHashMap();
            resultMap.put("orderDate", orderDate);
            return Response.success("success", resultMap);
        } catch (Exception e) {
            LOGGER.error("检测最新赊购信息失败", e);
            return Response.fail(BusinessErrorCode.NO);
        }
    }
}
