package com.ocean.piuda.admin.common.enums;

public enum ActivityType {
    TRANSPLANT,              // 이식
    GRAZER_REMOVAL,          // 조식동물 작업
    SUBSTRATE_IMPROVEMENT,   // 부착기질 개선
    MONITORING,              // 모니터링
    MARINE_CLEANUP,          // 해양정화
    // 하위 호환성을 위한 기존 값들
    TRASH_COLLECTION,        // 폐기물수거 (MARINE_CLEANUP과 동일)
    RESEARCH,                // 연구
    URCHIN_REMOVAL,          // 성게 제거 (GRAZER_REMOVAL과 동일)
    OTHER                    // 기타
}
