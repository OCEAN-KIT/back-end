package com.ocean.piuda.bio.dto.response;

import com.ocean.piuda.bio.entity.Species;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record SpeciesResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {
    public static SpeciesResponse from(Species species) {
        return SpeciesResponse.builder()
                .id(species.getId())
                .name(species.getName())
                .createdAt(species.getCreatedAt())
                .build();
    }
}