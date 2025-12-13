package com.ocean.piuda.admin.report.util;

import com.ocean.piuda.admin.report.enums.ReportDraftType;

public class ReportPromptBuilder {

    private ReportPromptBuilder() {}

    public static String build(String submissionsJson, String extraPromptOrNull, ReportDraftType type) {
        String extra = (extraPromptOrNull == null) ? "" : extraPromptOrNull.trim();
        String key = type.jsonKey();

        String guide = switch (type) {
            case INTERNAL_DRAFT -> """
- internalDraft: 기간/범위, 활동유형별 요약, 정량(수거량/이식수 등), 운영/이슈 중심.
- (중요) 데이터에 없거나 활동유형에 비적용인 지표는 "기록 없음"으로 쓰지 말고 항목 자체를 생략한다.
- 누락이 운영상 문제로 보이는 경우에만 "확인 필요"로 짧게 표시한다.
""";
            case EXTERNAL_NEWSLETTER -> """
- externalNewsletter: 짧은 제목 + 2~4문단 + 하이라이트 bullet + CTA.
""";
            case EXTERNAL_INSTAGRAM -> """
- externalInstagram: 4~10줄 스토리텔링 + 핵심 bullet 2~4개 + 해시태그 8~15개.
""";
            case EXTERNAL_PUBLICATION -> """
- externalPublication: 제목/리드문/본문(소제목 가능) + 성과 요약.
""";
        };

        return """
너는 해양 활동 기록 제출물(Submission)을 바탕으로 리포트 초안을 작성하는 전문 에디터다.

[중요 원칙]
- 아래에 제공된 JSON 데이터에 포함된 사실만 사용한다. 추측/날조 금지.
- 데이터가 부족하면 INTERNAL_DRAFT는 필요한 경우에만 "확인 필요"로 표시하고, 그 외 유형은 "확인 필요" 또는 "기록 없음"으로 명시한다.
- 외부홍보용(external*)에는 개인정보/민감정보를 포함하지 말 것:
  - 이메일/연락처 금지
  - 위도/경도 등 정확 좌표 금지(지역명 수준으로만)
- 문서는 모두 한국어로 작성한다.
- 결과는 반드시 "JSON만" 출력한다. 코드펜스( ``` ) 금지.
- 아래에서 지정한 키 1개만 출력한다. 다른 키/설명/문장 절대 추가 금지.

[데이터]
%s

[출력 JSON 스키마] (반드시 이 키 하나만)
{ "%s": "요청된 유형의 마크다운 초안" }

[작성 가이드]
%s

[추가 지시사항(있으면 반영)]
%s
""".formatted(submissionsJson, key, guide, extra.isBlank() ? "(없음)" : extra);
    }
}
