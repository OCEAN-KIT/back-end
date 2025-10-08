package com.ocean.piuda.payment.enums;

public enum PaymentStatus {
    CREATED,          // 결제 객체만 생성
    RESERVED,         // 금액 예약
    PENDING,          // PG/은행 응답 대기
    APPROVED,         // 사장 승인
    PAID,             // 최종 결제 완료
    CANCELED,         // 승인 전 취소
    REFUNDED,         // 환불 완료
    FAILED            // 실패
}
