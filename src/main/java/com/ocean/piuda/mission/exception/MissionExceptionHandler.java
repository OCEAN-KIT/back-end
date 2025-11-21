package com.ocean.piuda.mission.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Mission 도메인 예외 처리 핸들러
 */
@RestControllerAdvice(basePackages = "com.ocean.piuda.mission.controller")
public class MissionExceptionHandler {

    @ExceptionHandler(MissionNotFoundException.class)
    public ResponseEntity<String> handleMissionNotFound(MissionNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(MissionAccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(MissionAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }
}

