package com.ocean.piuda.store.entity.embeded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Builder
@Getter
@Embeddable
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column
    private Double latitude; // 위도

    @Column
    private Double longitude; // 경도

    @Column
    private String roadAddress; // 도로명 주소

    @Column
    private String detailAddress; // 상세 주소 (예: 101호)

    public String getFullAddress() {
        if (detailAddress == null || detailAddress.isBlank()) {
            return roadAddress;
        }
        return roadAddress + " " + detailAddress;
    }
}
