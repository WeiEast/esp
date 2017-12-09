package com.apass.esp.domain.enums;

public enum SourceType {


	JD("jd", "京东"),
	WZ("wz", "微知");

	private String code;

	private String message;

	private SourceType(String code, String message) {
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
