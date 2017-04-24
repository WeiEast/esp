package com.apass.esp.repository.httpClient;

import java.util.Map;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.apass.esp.domain.Response;
import com.apass.gfb.framework.utils.GsonUtils;
import com.apass.gfb.framework.utils.HttpClientUtils;

public class EspActivityHttpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(EspActivityHttpClient.class);

	@Value("${gfbwechat.request.address}")
	public String gfbReqUrl;

	@Value("${gfb.service.url}")
	public String gfbAppReqUrl;

	// 绑定卡片
	private static final String BIND_CARD_URL = "/espReWardActivity/bindCard";

	// 验卡是否本人 以及是否支持该银行
	private static final String BIND_CARD_IMFORMATION_URL = "/espReWardActivity/validateBindCard";

	// 银行卡列表
	private static final String BIND_LIST_URL = "/espReWardActivity/bankList";

	private static final String IDENTITY_RECONIZE_URL = "/espReWardActivity/identityReconize";

	/**
	 * 验卡是否本人 以及是否支持该银行
	 * 
	 * @param map
	 * @return
	 */
	public Response validateBindCard(Map<String, Object> map) {
		String requestUrl = gfbReqUrl + BIND_CARD_IMFORMATION_URL;
		String requestJson = GsonUtils.toJson(map);
		StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
		try {
			String responseJson = HttpClientUtils.getMethodPostResponse(requestUrl, entity);
			LOGGER.info("验卡是否本人 以及是否支持该银行:{}", responseJson);
			Response result = GsonUtils.convertObj(responseJson, Response.class);
			return result;
		} catch (Exception e) {
			LOGGER.error("验卡是否本人 以及是否支持该银行接口调用异常:{}", e);
			return Response.fail("验卡是否本人 以及是否支持该银行接口调用异常");
		}

	}

	/**
	 * 绑卡
	 * 
	 * @param map
	 * @return
	 */
	public Response bindCard(Map<String, Object> map) {
		String requestUrl = gfbReqUrl + BIND_CARD_URL;
		String requestJson = GsonUtils.toJson(map);
		StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
		try {
			String responseJson = HttpClientUtils.getMethodPostResponse(requestUrl, entity);
			LOGGER.info("绑卡:{}", responseJson);
			Response result = GsonUtils.convertObj(responseJson, Response.class);
			return result;
		} catch (Exception e) {
			LOGGER.error("绑卡接口调用异常:{}", e);
			return Response.fail("绑卡接口调用异常");
		}
	}

	/**
	 * 
	 * 银行卡列表
	 * 
	 * @param map
	 * @return
	 */
	public Response getBankList(Map<String, Object> map) {
		String requestUrl = gfbReqUrl + BIND_LIST_URL;
		String requestJson = GsonUtils.toJson(map);
		StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
		try {
			String responseJson = HttpClientUtils.getMethodPostResponse(requestUrl, entity);
			LOGGER.info("银行卡列表:{}", responseJson);
			Response result = GsonUtils.convertObj(responseJson, Response.class);
			return result;
		} catch (Exception e) {
			LOGGER.error("银行卡列表接口调用异常:{}", e);
			return Response.fail("银行卡列表接口调用异常");
		}
	}

	/**
	 * 上传并解析身份证图片
	 * 
	 * @param map
	 * @return
	 */
	public Response identityReconize(Map<String, Object> map) {
		String requestUrl = gfbAppReqUrl + IDENTITY_RECONIZE_URL;
		String requestJson = GsonUtils.toJson(map);
		StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
		try {
			String responseJson = HttpClientUtils.getMethodPostResponse(requestUrl, entity);
			LOGGER.info("上传并解析身份证图片:{}", responseJson);
			Response result = GsonUtils.convertObj(responseJson, Response.class);
			return result;
		} catch (Exception e) {
			LOGGER.error("上传并解析身份证图片接口调用异常:{}", e);
			return Response.fail("上传并解析身份证图片接口调用异常");
		}
	}
}
