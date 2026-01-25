package com.ocean.piuda.bio.dto.response;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.bio.enums.BioGroup;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record SpeciesResponse(
        Long id,
        String name,
        BioGroup category,
        LocalDateTime createdAt
) {
    public static SpeciesResponse from(Species species) {
        return SpeciesResponse.builder()
                .id(species.getId())
                .name(species.getName())
                .category(species.getCategory())
                .createdAt(species.getCreatedAt())
                .build();
    }
}