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
public class Survival {

    @Column(name = "surv_die_count")
    private Float dieCount;     // 죽은 개체수

    @Column(name = "surv_total_count")
    private Float totalCount;   // 총 개체수
}