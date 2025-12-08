package com.ocean.piuda.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AreaStatResponse {
    private long totalCount;      // 영역 내 프로젝트 수
    private double totalAreaSize; // 총 복원 면적 합계 (m2)
    private double avgDepth;      // 평균 수심 (m)
}