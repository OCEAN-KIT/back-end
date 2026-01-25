package com.ocean.piuda.bio.service;

import com.ocean.piuda.bio.dto.request.SpeciesRequest;
import com.ocean.piuda.bio.dto.response.SpeciesResponse;
import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.bio.repository.SpeciesRepository;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpeciesService {

    private final SpeciesRepository speciesRepository;

    /** DTO를 기반으로 엔티티 생성 후 저장 */
    public SpeciesResponse createSpecies(SpeciesRequest.Create req) {
        speciesRepository.findByName(req.name()).ifPresent(s -> {
            throw new BusinessException(ExceptionType.DUPLICATE_VALUE_ERROR);
        });

        Species species = Species.builder()
                .name(req.name())
                .category(req.category())
                .build();

        return SpeciesResponse.from(speciesRepository.save(species));
    }

    @Transactional(readOnly = true)
    public List<SpeciesResponse> getAllSpecies() {
        return speciesRepository.findAll().stream()
                .map(SpeciesResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpeciesResponse> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllSpecies();
        }
        return speciesRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(SpeciesResponse::from)
                .toList();
    }

    /** 수정 로직에 DTO 적용 */
    public void updateSpecies(Long id, SpeciesRequest.Update req) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        if (req.name() != null && !req.name().equals(species.getName())) {
            speciesRepository.findByName(req.name()).ifPresent(s -> {
                throw new BusinessException(ExceptionType.DUPLICATE_VALUE_ERROR);
            });
        }

        species.update(req.name(), req.category());
    }

    public void deleteSpecies(Long id) {
        if (!speciesRepository.existsById(id)) {
            throw new BusinessException(ExceptionType.RESOURCE_NOT_FOUND);
        }
        speciesRepository.deleteById(id);
    }
}