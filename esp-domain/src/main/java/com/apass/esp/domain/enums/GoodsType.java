package com.apass.esp.domain.enums;

/**
 * 说明：商品类型枚举，1表示正常，2表示精选
 * 
 * @author xiaohai
 * @version 1.0
 * @date 2016年12月23日
 */
public enum GoodsType {

	GOOD_NORMAL("1", "正常"),

	GOOD_SIFT("2", "精选");

	private String code;

	private String message;

	private GoodsType(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
