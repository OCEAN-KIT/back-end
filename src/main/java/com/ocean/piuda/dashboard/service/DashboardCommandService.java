package com.ocean.piuda.dashboard.service;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.bio.repository.SpeciesRepository;
import com.ocean.piuda.dashboard.dto.request.*;
import com.ocean.piuda.dashboard.entity.*;
import com.ocean.piuda.dashboard.repository.*;
import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardCommandService {

    private final ProjectAreaRepository projectAreaRepository;
    private final TransplantLogRepository transplantLogRepository;
    private final GrowthLogRepository growthLogRepository;
    private final WaterLogRepository waterLogRepository;
    private final MediaLogRepository mediaLogRepository;
    private final SpeciesRepository speciesRepository;

    // -------------------------
    // ProjectArea
    // -------------------------

    public Long createArea(CreateProjectAreaRequest req) {
        ProjectArea area = ProjectArea.builder()
                .name(req.name())
                .restorationRegion(req.restorationRegion())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .habitat(req.habitat())
                .depth(req.depth())
                .areaSize(req.areaSize())
                .level(req.level())
                .attachmentStatus(req.attachmentStatus())
                .build();

        area.setLocation(req.lat(), req.lon());
        return projectAreaRepository.save(area).getId();
    }

    public void updateArea(Long areaId, UpdateProjectAreaRequest req) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));


        Species representativeSpecies = (req.representativeSpeciesId() != null ) ? speciesRepository.findById(req.representativeSpeciesId())
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND)) : null;

        area.update(
                req.name(),
                req.restorationRegion(),
                req.startDate(),
                req.endDate(),
                req.habitat(),
                req.depth(),
                req.areaSize(),
                req.level(),
                req.attachmentStatus(),
                req.lat(),
                req.lon(),
                representativeSpecies
        );
    }

    public void deleteArea(Long areaId) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        projectAreaRepository.delete(area); // cascade로 로그들도 같이 정리
    }

    // -------------------------
    // TransplantLog
    // -------------------------

    public Long createTransplant(Long areaId, CreateTransplantLogRequest req) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Species species = speciesRepository.findById(req.speciesId())
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        TransplantLog log = TransplantLog.builder()
                .recordDate(req.recordDate())
                .method(req.method())
                .species(species)
                .count(req.count())
                .areaSize(req.areaSize())
                .attachmentStatus(req.attachmentStatus())
                .build();

        area.addTransplant(log);

        return transplantLogRepository.save(log).getId();
    }

    public void updateTransplant(Long areaId, Long logId, UpdateTransplantLogRequest req) {
        TransplantLog log = transplantLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Species species = null;
        if (req.speciesId() != null) {
            species = speciesRepository.findById(req.speciesId())
                    .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        }

        log.update(
                req.recordDate(),
                req.method(),
                species,
                req.count(),
                req.areaSize(),
                req.attachmentStatus()
        );
    }

    public void deleteTransplant(Long areaId, Long logId) {
        TransplantLog log = transplantLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        transplantLogRepository.delete(log);
    }


    // -------------------------
    // ProjectArea - Representative Species
    // -------------------------
    public void setRepresentativeSpecies(Long areaId, SetRepresentativeSpeciesRequest req) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Species species = speciesRepository.findById(req.speciesId())
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        area.setRepresentativeSpecies(species);
    }

    // -------------------------
    // GrowthLog
    // -------------------------

    public Long createGrowth(Long areaId, CreateGrowthLogRequest req) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Species species = speciesRepository.findById(req.speciesId())
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        GrowthLog log = GrowthLog.builder()
                .species(species)
                .recordDate(req.recordDate())
                .attachmentRate(req.attachmentRate())
                .survivalRate(req.survivalRate())
                .growthLength(req.growthLength())
                .status(req.status())
                .build();

        area.addGrowth(log);

        return growthLogRepository.save(log).getId();
    }

    public void updateGrowth(Long areaId, Long logId, UpdateGrowthLogRequest req) {
        GrowthLog log = growthLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        Species species = null;
        if (req.speciesId() != null) {
            species = speciesRepository.findById(req.speciesId())
                    .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        }

        log.update(
                species,
                req.recordDate(),
                req.attachmentRate(),
                req.survivalRate(),
                req.growthLength(),
                req.status()
        );
    }


    public void deleteGrowth(Long areaId, Long logId) {
        GrowthLog log = growthLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        growthLogRepository.delete(log);
    }

    // -------------------------
    // WaterLog
    // -------------------------

    public Long createWater(Long areaId, CreateWaterLogRequest req) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        WaterLog log = WaterLog.builder()
                .recordDate(req.recordDate())
                .temperature(req.temperature())
                .visibility(req.visibility())
                .current(req.current())
                .surge(req.surge())
                .wave(req.wave())
                .build();

        area.addWater(log);

        return waterLogRepository.save(log).getId();
    }

    public void updateWater(Long areaId, Long logId, UpdateWaterLogRequest req) {
        WaterLog log = waterLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        log.update(
                req.recordDate(),
                req.temperature(),
                req.visibility(),
                req.current(),
                req.surge(),
                req.wave()
        );
    }


    public void deleteWater(Long areaId, Long logId) {
        WaterLog log = waterLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        waterLogRepository.delete(log);
    }

    // -------------------------
    // MediaLog
    // -------------------------

    public Long createMedia(Long areaId, CreateMediaLogRequest req) {
        ProjectArea area = projectAreaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        MediaLog log = MediaLog.builder()
                .recordDate(req.recordDate())
                .mediaUrl(req.mediaUrl())
                .caption(req.caption())
                .category(req.category())
                .build();

        area.addMedia(log);

        return mediaLogRepository.save(log).getId();
    }

    public void updateMedia(Long areaId, Long logId, UpdateMediaLogRequest req) {
        MediaLog log = mediaLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));

        log.update(
                req.recordDate(),
                req.mediaUrl(),
                req.caption(),
                req.category()
        );
    }


    public void deleteMedia(Long areaId, Long logId) {
        MediaLog log = mediaLogRepository.findByIdAndProjectAreaId(logId, areaId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESOURCE_NOT_FOUND));
        mediaLogRepository.delete(log);
    }
}
