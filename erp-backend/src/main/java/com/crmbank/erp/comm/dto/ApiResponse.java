/*
 * ===========================================
 * 프로그램명   : -
 * 프로그램 ID  : -
 * 작성일자     : 25.08.13
 * 작성자      : 이현준
 * 수정일자    : -
 * 수정자      : -
 * 설명        : 공통 API 응답
 * 수정 내용    : -
 * ===========================================
 */

package com.crmbank.erp.comm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private int status;
	private String message;
	private T data;
	private String timestamp;

	/*
	* 성공 응답 (데이터 및 메시지 반환)
	* */
	public static <T> ApiResponse<T> success(T data, String message) {
		return ApiResponse.<T>builder()
			.status(200)
			.message(message)
			.data(data)
			.timestamp(LocalDateTime.now().toString())
			.build();
	}

	/*
	* 성공 응답 (메시지 반환)
	* */
	public static <T> ApiResponse<T> success(String message) {
		return ApiResponse.<T>builder()
			.status(200)
			.message(message)
			.data(null)
			.timestamp(LocalDateTime.now().toString())
			.build();
	}

	public static <T> ApiResponse<T> error(int status, String message) {
		return ApiResponse.<T>builder()
			.status(status)
			.message(message)
			.data(null)
			.timestamp(LocalDateTime.now().toString())
			.build();
	}

	public static <T> ApiResponse<T> badRequest(String message) {
		return error(400, message);
	}

	public static <T> ApiResponse<T> unauthorized(String message) {
		return error(401, message);
	}

	public static <T> ApiResponse<T> forbidden(String message) {
		return error(403, message);
	}

	public static <T> ApiResponse<T> notFound(String message) {
		return error(404, message);
	}

	public static <T> ApiResponse<T> serverError(String message) {
		return error(500, message);
	}
}