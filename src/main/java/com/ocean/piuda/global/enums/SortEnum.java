package com.ocean.piuda.global.enums;

import org.springframework.data.domain.Sort;

/**
 * 정렬 기준을 정의하는 인터페이스
 * 각 도메인별로 이 인터페이스를 구현하여 고유한 정렬 기준을 정의할 수 있습니다.
 */
public interface SortEnum {

    /**
     * Spring Data의 Sort 객체로 변환합니다.
     * @return Sort 객체
     */
    Sort toSort();
}
