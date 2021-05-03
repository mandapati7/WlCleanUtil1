package com.fedex.oce.vo;

import com.fedex.oce.vo.ExecutionEnum.ExecutionCd;

public class ResultInfo {

	private ExecutionCd executionCd;

	private String message;

	public ResultInfo(ExecutionCd executionCd, String message) {
		this.executionCd = executionCd;
		this.message = message;
	}

	public ExecutionCd getExecutionCd() {
		return executionCd;
	}

	public String getMessage() {
		return message;
	}

}
