package com.ocean.piuda.global.api.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {
    // common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, "C001", "예상치못한 서버에러 발생"),
    BINDING_ERROR(BAD_REQUEST, "C002", "잘못된 바인딩/조합"),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT , "C003","필수적인 필드 부재"),
    INVALID_VALUE_ERROR(NOT_ACCEPTABLE , "C004","값이 유효하지 않음"),
    DUPLICATE_VALUE_ERROR(NOT_ACCEPTABLE , "C005","값이 중복됨"),
    NOT_VALID_REQUEST_FIELDS_ERROR(BAD_REQUEST , "C006","요청 필드 검증에 실패했습니다."),
    MEDIA_TYPE_MISMATCHED(BAD_REQUEST, "C007","잘못된 콘텐츠 타입 사용"),
    RESOURCE_NOT_FOUND(NOT_FOUND,"C008","해당 자원을 찾을 수 없습니다."),

    // auth
    INVALID_REFRESH_TOKEN(NOT_ACCEPTABLE , "A001","유효하지 않은 리프레시 토큰"),
    REFRESH_TOKEN_EXPIRED(UNAUTHORIZED,"A002","리프레시 토큰 만료"),
    PASSWORD_NOT_MATCHED(NOT_ACCEPTABLE , "A003","비밀번호 불일치"),
    ACCESS_DENIED(FORBIDDEN, "A004", "요청한 리소스에 대한 권한이 없습니다."),
    AUTH_PRINCIPAL_INVALID(UNAUTHORIZED,"A005","principal 관련 에러 발생"),

    // oauth2
    INVALID_PROVIDER_TYPE_ERROR(NOT_ACCEPTABLE , "O001","지원하지 않는 provider"),

    // user
    USER_NOT_FOUND(NOT_FOUND, "U001", "존재하지 않는 사용자"),
    DUPLICATED_USER_ID(CONFLICT, "U002", "중복 아이디(PK)"),
    DUPLICATED_USERNAME(CONFLICT, "U003", "중복 아이디(username)"),
    ALREADY_REGISTERED_USER(NOT_ACCEPTABLE , "U006","이미 최종 회원 가입된 사용자"),
    NOT_REGISTERED_USER(FORBIDDEN , "U007","최종 회원 가입 되지 않은 사용자"),
    UNAUTHORIZED_USER(UNAUTHORIZED, "U005","로그인 되지 않은 사용자"),



    //store
    STORE_NOT_FOUND(NOT_FOUND, "S001", "존재하지 않는 가게"),

    // admin
    SUBMISSION_NOT_FOUND(NOT_FOUND, "AD001", "제출 데이터를 찾을 수 없습니다."),
    SUBMISSION_ALREADY_APPROVED(CONFLICT, "AD002", "이미 승인된 제출입니다."),
    SUBMISSION_ALREADY_REJECTED(CONFLICT, "AD003", "이미 반려된 제출입니다."),
    SUBMISSION_ALREADY_DELETED(CONFLICT, "AD004", "이미 삭제된 제출입니다."),
    REJECT_REASON_REQUIRED(UNPROCESSABLE_ENTITY, "AD005", "반려 사유를 입력해주세요."),
    EXPORT_NOT_FOUND(NOT_FOUND, "AD006", "내보내기 작업을 찾을 수 없습니다."),
    EXPORT_NOT_READY(UNPROCESSABLE_ENTITY, "AD007", "내보내기 파일이 아직 준비되지 않았습니다."),
    EXPORT_FAILED(INTERNAL_SERVER_ERROR, "AD008", "내보내기 생성에 실패했습니다."),

    //mission
    MISSION_ACCESS_DENIED(FORBIDDEN, "M001", "해당 미션에 대한 접근 권한이 없습니다."),
    MISSION_NOT_FOUND(NOT_FOUND, "M002", "해당 미션을 찾을 수 없습니다."),

    //garmin
    WATCH_API_KEY_INVALID(UNAUTHORIZED, "W001", "유효하지 않은 시계 API 키"),
    INVALID_PAYLOAD(BAD_REQUEST, "W002", "유효하지 않은 페이로드")
            ;


    private final HttpStatus status;
    private final String code;
    private final String message;}
