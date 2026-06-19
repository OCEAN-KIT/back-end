package com.ocean.piuda.dashboard.dto.response;

import com.ocean.piuda.bio.entity.Species;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AreaSpeciesResponse {
    private Long speciesId;
    private String speciesName;

    public static AreaSpeciesResponse from(Species species) {
        return AreaSpeciesResponse.builder()
                .speciesId(species.getId())
                .speciesName(species.getName())
                .build();
    }
}