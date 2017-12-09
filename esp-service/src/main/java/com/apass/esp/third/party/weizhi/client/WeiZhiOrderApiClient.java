package com.apass.esp.third.party.weizhi.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.apass.esp.common.utils.JsonUtil;
import com.apass.esp.service.wz.WeiZhiTokenService;
import com.apass.esp.third.party.weizhi.entity.AddressInfo;
import com.apass.esp.third.party.weizhi.entity.OrderReq;
import com.apass.esp.third.party.weizhi.response.OrderInfoResponse;
import com.apass.esp.third.party.weizhi.response.OrderTrack;
import com.apass.esp.third.party.weizhi.response.OrderTrackResponse;
import com.apass.esp.third.party.weizhi.response.OrderUnitResponse;
import com.apass.esp.third.party.weizhi.response.TrackData;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.GsonUtils;
import com.apass.gfb.framework.utils.HttpClientUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
@Service
public class WeiZhiOrderApiClient {

	private static final Logger logger = LoggerFactory.getLogger(WeiZhiOrderApiClient.class);
	
	@Autowired
	private WeiZhiTokenService weiZhiTokenService;
	@Autowired
	private WeiZhiConstants weiZhiConstants;
	/**
     * 统一下单接口
     *
     * @param orderReq
     * @return
	 * @throws Exception 
     */
    public OrderUnitResponse submitOrder(OrderReq orderReq) throws Exception {
        Objects.requireNonNull(orderReq.getOrderPriceSnap());
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        
        if (orderReq.getSkuNumList().size() > 50) {
            throw new BusinessException("最大数量为50，当前:" + orderReq.getSkuNumList().size());
        }
        
        BasicNameValuePair param1 = new BasicNameValuePair("token", weiZhiTokenService.getTokenFromRedis());
        BasicNameValuePair param2 = new BasicNameValuePair("thirdOrder", orderReq.getOrderNo());
        BasicNameValuePair param3 = new BasicNameValuePair("sku", JsonUtil.toJsonString(orderReq.getSkuNumList()));
        AddressInfo addressInfo = orderReq.getAddressInfo();
        BasicNameValuePair param4 = new BasicNameValuePair("name", addressInfo.getReceiver());
        BasicNameValuePair param5 = new BasicNameValuePair("province", addressInfo.getProvinceId()+"");
        BasicNameValuePair param6 = new BasicNameValuePair("city", addressInfo.getCityId()+"");
        BasicNameValuePair param7 = new BasicNameValuePair("county", addressInfo.getCountyId()+"");
        BasicNameValuePair param8 = new BasicNameValuePair("town", addressInfo.getTownId()+"");
        BasicNameValuePair param9 = new BasicNameValuePair("address", addressInfo.getAddress());
        BasicNameValuePair param10 = new BasicNameValuePair("zip", "");
        BasicNameValuePair param11 = new BasicNameValuePair("phone", addressInfo.getPhone());
        BasicNameValuePair param12 = new BasicNameValuePair("mobile", addressInfo.getMobile());
        BasicNameValuePair param13 = new BasicNameValuePair("email", addressInfo.getEmail());
        BasicNameValuePair param14 = new BasicNameValuePair("remark", orderReq.getRemark());
        /*
		        下单价格模式
			0:客户端订单价格快照不做验证对比，还是以京东端价格正常下单;
			1:必需验证客户端订单价格快照，如果快照与京东端价格不一致返回下单失败，需要更新商品价格后，重新下单;
         */
        BasicNameValuePair param15 = new BasicNameValuePair("orderPriceSnap", JsonUtil.toJsonString(orderReq.getOrderPriceSnap()));
        parameters.add(param1);
        parameters.add(param2);
        parameters.add(param3);parameters.add(param4);
        parameters.add(param5);
        parameters.add(param6);parameters.add(param7);
        parameters.add(param8);parameters.add(param9);
        parameters.add(param10);
        parameters.add(param11);parameters.add(param12);
        parameters.add(param13);parameters.add(param14);
        parameters.add(param15);
        logger.info("----submitOrder------ params:{}",JsonUtil.toJsonString(parameters));
        
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
        String responseJson = HttpClientUtils.getMethodPostResponse(weiZhiConstants.getWZRequestUrl(WeiZhiConstants.WZAPI_ORDER_SUBMITORDER), entity);
        
        logger.info("----submitOrder------ response:{}",responseJson);
        /**
         * 返回json
         */
        JSONObject datas = JSON.parseObject(responseJson);
        
        if(null == datas){
        	logger.error("----submitOrder--- callback is null");
        	return null;
        }
        
        String result = datas.getString("result");
        
        if(!StringUtils.equals(result, "0") && !StringUtils.equals(result, "0008")){
        	String message = datas.getString("detail");
        	logger.error("---submitOrder---- callback result:{},message:{}",result,message);
        	return null;
        }
        
        String content = JSONObject.toJSONString(datas.getJSONObject("data"));
        
        OrderUnitResponse response = GsonUtils.convertObj(content, OrderUnitResponse.class);
        
        return response;
    }
    
    /**
     * 根据微知订单号，确认预占库存
     *
     * @return
     * @throws Exception 
     */
    public boolean orderOccupyStockConfirm(String wzOrderId) throws Exception {
    	
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		BasicNameValuePair param1 = new BasicNameValuePair("token", weiZhiTokenService.getTokenFromRedis());
		BasicNameValuePair param2 = new BasicNameValuePair("wzOrderId",wzOrderId);
		parameters.add(param1);
		parameters.add(param2);
		logger.info("----orderOccupyStockConfirm------ params:{}",JsonUtil.toJsonString(parameters));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
		
		String responseJson = HttpClientUtils.getMethodPostResponse(weiZhiConstants.getWZRequestUrl(WeiZhiConstants.WZAPI_ORDER_CONFIRMORDER), entity);
	    
		logger.info("----orderOccupyStockConfirm------ response:{}",responseJson);
	    /**
	     * 返回json
	     */
	    JSONObject datas = JSON.parseObject(responseJson);
	    
	    if(null == datas){
	    	logger.error("----orderOccupyStockConfirm--- callback is null");
	    	return false;
	    }
	    
	    String result = datas.getString("result");
	    
	    if(!StringUtils.equals(result, "0")){
	    	String message = datas.getString("detail");
	    	logger.error("---orderOccupyStockConfirm---- callback result:{},message:{}",result,message);
	    	return false;
	    }
	    
		return datas.getBooleanValue("data");
    }
    
    /**
     * 根据微知订单号，查询订单明细
     *
     * @param jdOrderId
     * @return
     * @throws Exception 
     */
    public OrderInfoResponse selectOrder(String wzOrderId) throws Exception {
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		BasicNameValuePair param1 = new BasicNameValuePair("token", weiZhiTokenService.getTokenFromRedis());
		BasicNameValuePair param2 = new BasicNameValuePair("wzOrderId",wzOrderId);
		parameters.add(param1);
		parameters.add(param2);
		logger.info("----selectOrder------ params:{}",JsonUtil.toJsonString(parameters));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
		
		String responseJson = HttpClientUtils.getMethodPostResponse(weiZhiConstants.getWZRequestUrl(WeiZhiConstants.WZAPI_ORDER_SELECTORDER), entity);
	    
		logger.info("----selectOrder------ response:{}",responseJson);
	    /**
	     * 返回json
	     */
	    JSONObject datas = JSON.parseObject(responseJson);
	    
	    if(null == datas){
	    	logger.error("----selectOrder--- callback is null");
	    	return null;
	    }
	    
	    String result = datas.getString("result");
	    
	    if(!StringUtils.equals(result, "0")){
	    	String message = datas.getString("detail");
	    	logger.error("---selectOrder---- callback result:{},message:{}",result,message);
	    	return null;
	    }
	    
	    JSONArray array = datas.getJSONArray("data");
	    
	    /**
	     * 虽然用数组返回，但是只可能有一条数据
	     */
	    String content = JSONObject.toJSONString(array.get(0));
	    
	    OrderInfoResponse response = GsonUtils.convertObj(content, OrderInfoResponse.class);
	    
        return response;
    }
    
    /**
     * 根据微知单号，查询配送信息
     *
     * @return
     * @throws Exception 
     */
    public OrderTrackResponse orderTrack(String wzOrderId) throws Exception {

    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		BasicNameValuePair param1 = new BasicNameValuePair("token", weiZhiTokenService.getTokenFromRedis());
		BasicNameValuePair param2 = new BasicNameValuePair("wzOrderId",wzOrderId);
		parameters.add(param1);
		parameters.add(param2);
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
		
		String responseJson = HttpClientUtils.getMethodPostResponse(weiZhiConstants.getWZRequestUrl(WeiZhiConstants.WZAPI_ORDER_ORDERTRACK), entity);
		
		logger.info("----orderTrack------ response:{}",responseJson);
		
		/**
	     * 返回json
	     */
	    JSONObject datas = JSON.parseObject(responseJson);
	    
	    if(null == datas){
	    	logger.error("----orderTrack--- callback is null");
	    	return null;
	    }
	    
	    String result = datas.getString("result");
	    
	    if(!StringUtils.equals(result, "0")){
	    	String message = datas.getString("detail");
	    	logger.error("---orderTrack---- callback result:{},message:{}",result,message);
	    	return null;
	    }
	    OrderTrackResponse response = new OrderTrackResponse();
	    List<OrderTrack> orderTrack = Lists.newArrayList();
	    JSONObject object = datas.getJSONObject("data");
	    String orderId = object.getString("wzOrderId");
	    response.setWzOrderId(orderId);
	    JSONArray array = object.getJSONArray("orderTrack");
	    /**
	     * 先把返回的数据按照key-value的格式保存一下
	     */
	    for (Object obj : array) {
	    	String content =  JSONObject.toJSONString(obj);
	    	Map<String,Object> ss = GsonUtils.convertObj(content,Map.class);
	    	for (String key : ss.keySet()) {
	    		OrderTrack track = new OrderTrack();
	    		track.setTrackId(key);//设置物流单号
				Map<String,String> hh = (Map)ss.get(key);
				for (String key1 : hh.keySet()) {
			    	List<String> keys =  Arrays.asList(key1.split(","));
			    	track.setSkuId(keys);
		    		Object value = hh.get(key1);
			    	if(value instanceof List){
			    		List<TrackData> trackList = (List<TrackData>) value;
			    		track.setTackList(trackList);
			    	}else if(value instanceof String){
			    	    track.setMassge((String)value);
			    	}else{
			    		track.setMassge("");
			    	}
			    	orderTrack.add(track);
			    }
			}
		}
	    response.setOrderTrack(orderTrack);
		return response;
    }
    
    /**
     * 订单反查接口:根据本地订单的编号，反查微知订单号
     * @param thirdOrder
     * @return
     * @throws Exception 
     */
    public String selectOrderIdByThirdOrder(String thirdOrder) throws Exception{
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		BasicNameValuePair param1 = new BasicNameValuePair("token", weiZhiTokenService.getTokenFromRedis());
		BasicNameValuePair param2 = new BasicNameValuePair("thirdOrder",thirdOrder);
		parameters.add(param1);
		parameters.add(param2);
		logger.info("----selectOrderIdByThirdOrder------ params:{}",JsonUtil.toJsonString(parameters));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
		
		String responseJson = HttpClientUtils.getMethodPostResponse(weiZhiConstants.getWZRequestUrl(WeiZhiConstants.WZAPI_ORDER_SELECTORDERIDBYTHIRDORDER), entity);
	    
		logger.info("----selectOrderIdByThirdOrder------ response:{}",responseJson);
	    /**
	     * 返回json
	     */
	    JSONObject datas = JSON.parseObject(responseJson);
	    
	    if(null == datas){
	    	logger.error("----selectOrderIdByThirdOrder--- callback is null");
	    	return "";
	    }
	    
	    String result = datas.getString("result");
	    
	    if(!StringUtils.equals(result, "0")){
	    	String message = datas.getString("detail");
	    	logger.error("---selectOrderIdByThirdOrder---- callback result:{},message:{}",result,message);
	    	return "";
	    }
	    return datas.getString("WzOrder");
    }
    
    /**
     * 取消未确认订单接口（未调用确认预占库存）
     * @param wzOrderId 微知订单号
     * @return
     * @throws Exception 
     */
    public boolean cancelOrder(String wzOrderId) throws Exception{
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		BasicNameValuePair param1 = new BasicNameValuePair("token", weiZhiTokenService.getTokenFromRedis());
		BasicNameValuePair param2 = new BasicNameValuePair("wzOrderId",wzOrderId);
		parameters.add(param1);
		parameters.add(param2);
		logger.info("----cancelOrder------ params:{}",JsonUtil.toJsonString(parameters));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
		
		String responseJson = HttpClientUtils.getMethodPostResponse(weiZhiConstants.getWZRequestUrl(WeiZhiConstants.WZAPI_ORDER_CANCEL), entity);
	    
		logger.info("----cancelOrder------ response:{}",responseJson);
	    /**
	     * 返回json
	     */
	    JSONObject datas = JSON.parseObject(responseJson);
	    
	    if(null == datas){
	    	logger.error("----cancelOrder--- callback is null");
	    	return false;
	    }
	    
	    String result = datas.getString("result");
	    
	    if(!StringUtils.equals(result, "0")){
	    	String message = datas.getString("detail");
	    	logger.error("---cancelOrder---- callback result:{},message:{}",result,message);
	    	return false;
	    }
	    return datas.getBooleanValue("data");
    }
}
