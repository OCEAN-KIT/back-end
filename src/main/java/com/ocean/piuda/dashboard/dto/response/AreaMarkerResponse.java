package com.ocean.piuda.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 지도 마커용 요약 DTO
 * - 상세 데이터 없이, 지도/리스트에 필요한 필드만 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class AreaMarkerResponse {

    private Long id;
    private String name;

    private Double lat;
    private Double lon;

    private LocalDate startDate;
    private Double depth;
    private Double areaSize;

    /** 서식지 한글 설명 (예: "암반리프") */
    private String habitat;

    /** 프로젝트 단계 한글 설명 (예: "이식 완료") */
    private String level;
}
