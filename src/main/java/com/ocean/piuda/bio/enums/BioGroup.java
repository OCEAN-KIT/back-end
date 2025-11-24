package com.ocean.piuda.bio.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 해양/수산 생물 그룹 태그 공통 enum.
 * - Taxon(종 정보)
 * - Mission(미션 도메인)
 * - Post(게시글 도메인)
 * 등에서 모두 이 타입을 사용합니다.
 */

@Getter
@AllArgsConstructor
public enum BioGroup {

    MACROALGAE("해조류"),
    SEAGRASS("해초류"),
    FISH("어류"),
    MOLLUSK("연체동물"),
    CRUSTACEAN("갑각류"),
    ECHINODERM("극피동물"),
    CNIDARIA("자포동물"),
    PORIFERA("해면동물"),
    POLYCHAETE("다모류"),
    BRYOZOAN("태형동물"),
    TUNICATE("피낭동물"),
    OTHER_VERTEBRATE("기타 척추동물");

    private final String description;

}

