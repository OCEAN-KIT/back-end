package com.ocean.piuda.global.api.exception;


import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionType exceptionType;
    private final Map<String, ?> details;


    // details 없음
    public BusinessException(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
        this.details = Map.of();
    }


    // details 있음
    public BusinessException(ExceptionType exceptionType, Map<String, ?> details) {
        this.exceptionType = exceptionType;
        this.details = details;
    }
}

