package com.ocean.piuda.global.api.exception;

import com.ocean.piuda.global.api.dto.ApiData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ApiData<Void> handleApplicationException(BusinessException e) {
    return ApiData.error(e.getExceptionType(), e.getDetails());
  }



  @ExceptionHandler(MethodArgumentNotValidException.class)
  public  ApiData<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    Map<String, String> details = new HashMap<>();
    List<FieldError> fieldErrors = e.getFieldErrors();
    fieldErrors.forEach(fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage()));
    return ApiData.error(ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR, details);

  }

  /** @AuthenticationPrincipal(expression=...) 평가/캐스팅 실패 */
  @ExceptionHandler({ SpelEvaluationException.class, EvaluationException.class, ClassCastException.class, IllegalArgumentException.class })
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiData<Void> handleAuthPrincipalBinding(Exception e) {
    return ApiData.error(ExceptionType.AUTH_PRINCIPAL_INVALID, Map.of("message",e.getMessage() ));
  }



  @ExceptionHandler(Exception.class)
  public ApiData<Void> exception(Exception e) {
    Map<String, Object> details = Map.of("message",  e.getMessage());
    return ApiData.error(ExceptionType.UNEXPECTED_SERVER_ERROR,details);
  }

}
