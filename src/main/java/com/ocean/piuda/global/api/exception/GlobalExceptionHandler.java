package com.ocean.piuda.global.api.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.piuda.global.api.dto.ApiData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ObjectMapper objectMapper;

  @ExceptionHandler(BusinessException.class)
  public Object handleApplicationException(
          BusinessException e,
          HttpServletRequest request,
          HttpServletResponse response
  ) {
    ApiData<Void> body = ApiData.error(e.getExceptionType(), e.getDetails());

    //  PDF Accept만 온 경우에도 JSON 바디를 강제로 내려줌(컨텐트 협상 우회)
    if (wantsPdfOnly(request)) {
      writeJson(response, e.getExceptionType().getStatus().value(), body);
      return null;
    }

    return ResponseEntity
            .status(e.getExceptionType().getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Object handleMethodArgumentNotValidException(
          MethodArgumentNotValidException e,
          HttpServletRequest request,
          HttpServletResponse response
  ) {
    Map<String, String> details = new HashMap<>();
    List<FieldError> fieldErrors = e.getFieldErrors();
    fieldErrors.forEach(fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage()));

    ApiData<Void> body = ApiData.error(ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR, details);

    if (wantsPdfOnly(request)) {
      writeJson(response, ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR.getStatus().value(), body);
      return null;
    }

    return ResponseEntity
            .status(ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
  }

  /** @AuthenticationPrincipal(expression=...) 평가/캐스팅 실패 */
  @ExceptionHandler({ SpelEvaluationException.class, EvaluationException.class, ClassCastException.class, IllegalArgumentException.class })
  public Object handleAuthPrincipalBinding(
          Exception e,
          HttpServletRequest request,
          HttpServletResponse response
  ) {
    ApiData<Void> body = ApiData.error(ExceptionType.AUTH_PRINCIPAL_INVALID, Map.of("message", e.getMessage()));

    if (wantsPdfOnly(request)) {
      writeJson(response, ExceptionType.AUTH_PRINCIPAL_INVALID.getStatus().value(), body);
      return null;
    }

    return ResponseEntity
            .status(ExceptionType.AUTH_PRINCIPAL_INVALID.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
  }

  @ExceptionHandler(Exception.class)
  public Object exception(
          Exception e,
          HttpServletRequest request,
          HttpServletResponse response
  ) {
    log.error("Unhandled exception", e);
    ApiData<Void> body = ApiData.error(ExceptionType.UNEXPECTED_SERVER_ERROR, Map.of("message", e.getMessage()));

    if (wantsPdfOnly(request)) {
      writeJson(response, ExceptionType.UNEXPECTED_SERVER_ERROR.getStatus().value(), body);
      return null;
    }

    return ResponseEntity
            .status(ExceptionType.UNEXPECTED_SERVER_ERROR.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
  }

  private boolean wantsPdfOnly(HttpServletRequest request) {
    String accept = request.getHeader(HttpHeaders.ACCEPT);
    if (accept == null) return false;

    String a = accept.toLowerCase();
    boolean acceptsPdf = a.contains("application/pdf");
    boolean acceptsJson = a.contains("application/json") || a.contains("*/*");

    // pdf는 받겠는데 json은 안 받는 케이스(= Swagger에서 흔함)
    return acceptsPdf && !acceptsJson;
  }

  private void writeJson(HttpServletResponse response, int statusCode, ApiData<Void> body) {
    if (response.isCommitted()) return;

    try {
      response.setStatus(statusCode);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      String json = objectMapper.writeValueAsString(body);
      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

      response.setContentLength(bytes.length);
      response.getOutputStream().write(bytes);
      response.getOutputStream().flush();
    } catch (IOException io) {
      // 여기서 또 예외 던지면 더 꼬일 수 있어서 로그만
      log.error("Failed to write JSON error response", io);
    }
  }
}
