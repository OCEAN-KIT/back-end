package com.ocean.piuda.admin.submission.entity.embeded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaturalReproduction {

    @Column(name = "nat_radius_m")
    private Float radiusM;      // 관측 반경

    @Column(name = "nat_numerator")
    private Float numerator;    // 자연번식 개체수

    @Column(name = "nat_denominator")
    private Float denominator;  // 기준 개체수
}