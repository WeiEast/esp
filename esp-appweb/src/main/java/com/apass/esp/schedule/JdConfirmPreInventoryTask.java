package com.apass.esp.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.apass.esp.common.code.BusinessErrorCode;
import com.apass.esp.domain.entity.goods.GoodsInfoEntity;
import com.apass.esp.domain.entity.merchant.MerchantInfoEntity;
import com.apass.esp.domain.entity.order.OrderDetailInfoEntity;
import com.apass.esp.domain.entity.order.OrderInfoEntity;
import com.apass.esp.domain.enums.PaymentStatus;
import com.apass.esp.domain.enums.SourceType;
import com.apass.esp.repository.goods.GoodsRepository;
import com.apass.esp.repository.order.OrderDetailInfoRepository;
import com.apass.esp.repository.order.OrderInfoRepository;
import com.apass.esp.service.common.CommonService;
import com.apass.esp.service.goods.GoodsService;
import com.apass.esp.service.merchant.MerchantInforService;
import com.apass.esp.service.order.OrderService;
import com.apass.esp.third.party.jd.client.JdApiResponse;
import com.apass.esp.third.party.jd.client.JdOrderApiClient;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.logstash.LOG;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * type: class
 * 确认预占库存，拆单情况处理
 *
 * @author xianzhi.wang
 * @see
 * @since JDK 1.8
 */

@Component
@Configurable
@EnableScheduling
//@Profile("Schedule")
public class JdConfirmPreInventoryTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdConfirmPreInventoryTask.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdOrderApiClient jdOrderApiClient;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private MerchantInforService merchantInforService;

    @Autowired
    private CommonService commonService;

    @Autowired
    public OrderInfoRepository orderInfoRepository;

    @Autowired
    public OrderDetailInfoRepository orderDetailInfoRepository;

    @Autowired
    public GoodsRepository goodsDao;

    @Scheduled(cron = "0 0/30 * * * *")
    public void handleJdConfirmPreInventoryTask() {

        List<OrderInfoEntity> orderInfoEntityList = orderService.getOrderByOrderStatusAndPreStatus();
        if (CollectionUtils.isEmpty(orderInfoEntityList)) {
            return;
        }
        for (OrderInfoEntity orderInfoEntity : orderInfoEntityList
                ) {
            String jdOrderIdp = orderInfoEntity.getExtOrderId();
            LOGGER.info(" JdConfirmPreInventoryTask  jdOrderIdp {}  begin....", jdOrderIdp);
            long jdOrderId = Long.valueOf(jdOrderIdp);
            JdApiResponse<Boolean> confirmResponse = jdOrderApiClient.orderOccupyStockConfirm(jdOrderId);
            LOGGER.info("confirm order jdOrderIdp {} confirmResponse {}", jdOrderIdp, confirmResponse.toString());
            //int confirmStatus = 0;
            if (confirmResponse.isSuccess() && confirmResponse.getResult()) {
                JdApiResponse<JSONObject> jdApiResponse = jdOrderApiClient.orderJdOrderQuery(jdOrderId);
                if (!jdApiResponse.isSuccess()) {
                    LOGGER.info("confirm order jdOrderIdp {} confirmResponse {} orderJdOrderQuery result {}", jdOrderIdp, confirmResponse.toString(), jdApiResponse);
                    continue;
                }
                JSONObject jsonObject = jdApiResponse.getResult();
                Object pOrderV = jsonObject.get("pOrder");
                if (pOrderV instanceof Number) {
                    //拆单消息mq接收

                    //long pOrderId = ((Number) pOrderV).longValue();
                } else {
                    String merchantCode = orderInfoEntity.getMerchantCode();
                    String deviceType = orderInfoEntity.getDeviceType();
                    MerchantInfoEntity merchantInfoEntity = merchantInforService.queryByMerchantCode(merchantCode);
                    //拆单
                    JSONObject pOrderJsonObject = (JSONObject) pOrderV;
                    //父订单状态
                    pOrderJsonObject.getIntValue("type");
                    pOrderJsonObject.getIntValue("submitState");
                    pOrderJsonObject.getIntValue("orderState");

                    JSONArray cOrderArray = jsonObject.getJSONArray("cOrder");
                    for (int i = 0; i < cOrderArray.size(); i++) {
                        JSONObject cOrderJsonObject = cOrderArray.getJSONObject(i);
                        if (cOrderJsonObject.getLongValue("pOrder") != jdOrderId) {
                            LOGGER.info("cOrderJsonObject.getLongValue(\"pOrder\") {}, jdOrderId", cOrderJsonObject.getLongValue("pOrder"), jdOrderId);
                        }
                        long cOrderId = cOrderJsonObject.getLongValue("jdOrderId");//京东子订单ID
                        JSONArray cOrderSkuList = cOrderJsonObject.getJSONArray("sku");
                        BigDecimal jdPrice = BigDecimal.ZERO;//订单金额
                        Integer sumNum = 0;
                        for (int j = 0; j < cOrderSkuList.size(); j++) {
                            BigDecimal price = cOrderSkuList.getJSONObject(j).getBigDecimal("price");
                            int num = cOrderSkuList.getJSONObject(j).getIntValue("num");
                            jdPrice = jdPrice.add(price.multiply(new BigDecimal(num)));
                            sumNum = sumNum + num;
                        }
                        //创建新的订单号
                        String cOrderQh = commonService.createOrderIdNew(deviceType, merchantInfoEntity.getId());
                        //拆单创建新的订单
                        OrderInfoEntity orderInfo = new OrderInfoEntity();
                        orderInfo.setUserId(orderInfoEntity.getUserId());
                        orderInfo.setOrderAmt(jdPrice);
                        orderInfo.setSource(SourceType.JD.getCode());
                        orderInfo.setExtParentId(-1);//为子订单
                        orderInfo.setDeviceType(deviceType);
                        orderInfo.setOrderId(cOrderQh);
                        orderInfo.setGoodsNum(Long.valueOf(sumNum));
                        orderInfo.setPayStatus(PaymentStatus.PAYSUCCESS.getCode());
                        orderInfo.setProvince(orderInfoEntity.getProvince());
                        orderInfo.setCity(orderInfoEntity.getCity());
                        orderInfo.setDistrict(orderInfoEntity.getDistrict());
                        orderInfo.setAddress(orderInfoEntity.getAddress());
                        orderInfo.setPostcode(orderInfoEntity.getPostcode());
                        orderInfo.setName(orderInfoEntity.getName());
                        orderInfo.setTelephone(orderInfoEntity.getTelephone());
                        orderInfo.setMerchantCode(merchantCode);
                        orderInfo.setExtendAcceptGoodsNum(orderInfoEntity.getExtendAcceptGoodsNum());
                        orderInfo.setAddressId(orderInfoEntity.getAddressId());
                        orderInfo.setPreDelivery(orderInfoEntity.getPreDelivery());
                        orderInfo.setCreateDate(orderInfoEntity.getCreateDate());
                        orderInfo.setUpdateDate(new Date());
                        orderInfo.setExtOrderId(String.valueOf(cOrderId));
                        Integer successStatus = orderInfoRepository.insert(orderInfo);
                        if (successStatus < 1) {
                            LOGGER.info("jdOrderId {}  cOrderId {} cOrderQh {} create order error  ", jdOrderId, cOrderId, cOrderQh);
                            continue;
                        }
                        for (int j = 0; j < cOrderSkuList.size(); j++) {
                            long skuId = cOrderSkuList.getJSONObject(j).getLongValue("skuId");
                            GoodsInfoEntity goodsInfoEntity = goodsService.selectGoodsByExternalId(String.valueOf(skuId));
                            if (goodsInfoEntity == null) {
                                LOGGER.info("pOrder {}, jdOrderId {} goodsInfoEntity {}", cOrderJsonObject.getLongValue("pOrder"), jdOrderId, goodsInfoEntity);
                                continue;
                            }
                            long goodsId = goodsInfoEntity.getId();
                            BigDecimal price = cOrderSkuList.getJSONObject(j).getBigDecimal("price");
                            int num = cOrderSkuList.getJSONObject(j).getIntValue("num");
                            String name = cOrderSkuList.getJSONObject(j).getString("name");
                            GoodsInfoEntity goods = goodsDao.select(goodsId);
                            //orderDetail插入对应记录
                            OrderDetailInfoEntity orderDetail = new OrderDetailInfoEntity();
                            orderDetail.setOrderId(cOrderQh);
                            orderDetail.setGoodsId(goodsId);
                            orderDetail.setSkuId(String.valueOf(skuId));
                            orderDetail.setSource(SourceType.JD.getCode());
                            orderDetail.setGoodsPrice(price);
                            orderDetail.setGoodsNum(Long.valueOf(num));
                            orderDetail.setMerchantCode(merchantCode);
                            orderDetail.setGoodsTitle(goods.getGoodsTitle());
                            orderDetail.setCategoryCode(goods.getCategoryCode());
                            orderDetail.setGoodsName(goods.getGoodsName());
                            orderDetail.setGoodsSellPt(goods.getGoodsSellPt());
                            orderDetail.setGoodsType(goods.getGoodsType());
                            orderDetail.setListTime(goods.getListTime());
                            orderDetail.setDelistTime(goods.getDelistTime());
                            orderDetail.setProDate(goods.getProDate());
                            orderDetail.setKeepDate(goods.getKeepDate());
                            orderDetail.setSupNo(goods.getSupNo());
                            orderDetail.setCreateDate(new Date());
                            Integer orderDetailSuccess = orderDetailInfoRepository.insert(orderDetail);
                            if (orderDetailSuccess < 1) {
                                LOGGER.info("jdOrderId {}  cOrderId {} cOrderQh {} create order detail error  ", jdOrderId, cOrderId, cOrderQh);
                                continue;
                            }
                        }
                    }

                }

            } else {
                LOGGER.info("confirm order jdOrderIdp {}  error confirmResponse: {}", jdOrderIdp, confirmResponse);
            }
        }
    }

}