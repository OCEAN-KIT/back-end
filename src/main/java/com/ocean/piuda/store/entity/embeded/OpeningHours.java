package com.ocean.piuda.store.entity.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalTime;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class OpeningHours {
    // 자동 오픈/마감 등의 기능 x 가게 정보로 사용

    private LocalTime openTime;
    private LocalTime closeTime;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    private String holyDay; // 별도 테이블 없이 문자열 저장 ex) 매주 월요일
}
