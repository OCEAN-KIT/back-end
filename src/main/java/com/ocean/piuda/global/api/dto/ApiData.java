package com.ocean.piuda.global.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * ApiData 는 JSON 처리 전용
 * 기타 CSV 나 파일은 컨트롤러에서 byte[] 로 직접 반환 처리
 */
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
@ToString
public class ApiData<T> {

  private static final String SUCCESS_CODE = "0";
  private static final String SUCCESS_MESSAGE = "요청에 성공했습니다.";


  @JsonIgnore
  private HttpStatus httpStatus;

  @JsonIgnore
  private final List<ApiHeader> headers = new ArrayList<>();

  @JsonIgnore
  private MediaType contentType;

  private boolean success;
  private T data;
  private Map<String, ?> errors;
  private String code;
  private Object message;


  public static <T> ApiData<T> ok(T data) {
    return ApiData.<T>builder()
        .httpStatus(HttpStatus.OK)
        .success(true)
        .data(data)
        .contentType(MediaType.APPLICATION_JSON)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static <T> ApiData<T> created(T data) {
    return ApiData.<T>builder()
        .httpStatus(HttpStatus.CREATED)
        .success(true)
        .data(data)
        .contentType(MediaType.APPLICATION_JSON)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static ApiData<Void> noContent() {
        return ApiData.<Void>builder()
            .httpStatus(HttpStatus.NO_CONTENT)
            .success(true)
            .data(null)
            .contentType(MediaType.APPLICATION_JSON)
            .code(SUCCESS_CODE)
            .message(SUCCESS_MESSAGE)
            .build();
    }


  public static <T> ApiData<T> from(HttpStatus httpStatus, T data) {
    return ApiData.<T>builder()
        .httpStatus(httpStatus)
        .success(true)
        .data(data)
        .contentType(MediaType.APPLICATION_JSON)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static ApiData<Void> error(ExceptionType exceptionType) {
    return ApiData.error(exceptionType,Map.of());
  }

  public static ApiData<Void> error(ExceptionType exceptionType,Map<String, ?> details) {

    return ApiData.<Void>builder()
            .contentType(MediaType.APPLICATION_JSON)
            .success(false)
            .code(exceptionType.getCode())
            .message(exceptionType.getMessage())
            .httpStatus(exceptionType.getStatus())
            .data(null)                // 실패는 data 비움
            .errors(details == null ? Map.of() : details)
            .build();

  }



}
