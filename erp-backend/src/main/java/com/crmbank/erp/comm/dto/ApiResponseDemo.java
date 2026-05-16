/*
* 일단 Demo로 놔두고 나중에 제거할 예정.
* */

package com.crmbank.erp.comm.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDemo {
	private String message;
	private String statusCode;
	private String keyno; // 전달키
}
