package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.dashboard.entity.WaterLog;
import com.ocean.piuda.dashboard.enums.MarineStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class WaterLogResponse {
    private Long id;
    private LocalDate recordDate;

    private Double temperature;
    private Double dissolvedOxygen;
    private Double nutrient;

    private MarineStatus visibility;
    private String visibilityName;

    private MarineStatus current;
    private String currentName;

    private MarineStatus surge;
    private String surgeName;

    private MarineStatus wave;
    private String waveName;

    public static WaterLogResponse from(WaterLog w) {
        return WaterLogResponse.builder()
                .id(w.getId())
                .recordDate(w.getRecordDate())
                .temperature(w.getTemperature())
                .dissolvedOxygen(w.getDissolvedOxygen())
                .nutrient(w.getNutrient())
                .visibility(w.getVisibility())
                .visibilityName(w.getVisibility() != null ? w.getVisibility().getName() : null)
                .current(w.getCurrent())
                .currentName(w.getCurrent() != null ? w.getCurrent().getName() : null)
                .surge(w.getSurge())
                .surgeName(w.getSurge() != null ? w.getSurge().getName() : null)
                .wave(w.getWave())
                .waveName(w.getWave() != null ? w.getWave().getName() : null)
                .build();
    }
}