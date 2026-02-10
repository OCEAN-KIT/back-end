package com.ocean.piuda.dashboard.initializer;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.bio.repository.SpeciesRepository;
import com.ocean.piuda.dashboard.entity.*;
import com.ocean.piuda.dashboard.enums.*;
import com.ocean.piuda.dashboard.repository.ProjectAreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod")
@Order(1)
public class DashboardDataInitializer implements CommandLineRunner {

    private final ProjectAreaRepository projectAreaRepository;
    private final SpeciesRepository speciesRepository;

    private static final String VALID_IMAGE_URL = "/images/underSea.jpg";

    @Override
    @Transactional
    public void run(String... args) {
        if (projectAreaRepository.count() > 0) return;

        log.info("OC DASHBOARD 실증용 데이터 초기화 시작");

        // 1) Species
        Species gamtae = getOrCreateSpecies("감태");
        Species dasima = getOrCreateSpecies("다시마");
        Species mojaban = getOrCreateSpecies("모자반");

        // 2) Areas
        createPohangDemoArea1(gamtae, dasima, mojaban);
        createUljinDemoArea1(gamtae, dasima);

        log.info("OC DASHBOARD 데이터 초기화 완료");
    }

    private Species getOrCreateSpecies(String name) {
        return speciesRepository.findByName(name)
                .orElseGet(() -> speciesRepository.save(
                        Species.builder()
                                .name(name)
                                .build()
                ));
    }

    /**
     * 포항 테스트 영역 1
     */
    private void createPohangDemoArea1(Species gamtae, Species dasima, Species mojaban) {
        ProjectArea area = ProjectArea.builder()
                .name("포항 테스트 영역-1")
                .restorationRegion(RestorationRegion.POHANG)
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .habitat(HabitatType.ROCKY)
                .depth(10.5)
                .areaSize(1850.0)
                .level(ProjectLevel.GROWTH)
                .attachmentStatus(AreaAttachmentStatus.STABLE)
                .build();

        area.setLocation(36.0762, 129.4432);

        // [설정] 포항 영역 대표종: 감태
        area.setRepresentativeSpecies(gamtae);

        // -------- Transplant Logs --------
        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 3, 10))
                .species(gamtae)
                .method(TransplantMethod.SEEDLING_STRING)
                .count(48)
                .areaSize(500.0)
                .attachmentStatus(SpeciesAttachmentStatus.GOOD)
                .build());

        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 3, 18))
                .species(dasima)
                .method(TransplantMethod.TRANSPLANT_MODULE)
                .count(22)
                .areaSize(350.0)
                .attachmentStatus(SpeciesAttachmentStatus.NORMAL)
                .build());

        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 4, 5))
                .species(mojaban)
                .method(TransplantMethod.ROPE)
                .count(12)
                .areaSize(200.0)
                .attachmentStatus(SpeciesAttachmentStatus.GOOD)
                .build());

        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 6, 20))
                .species(mojaban)
                .method(TransplantMethod.ROPE)
                .count(8)
                .areaSize(120.0)
                .attachmentStatus(SpeciesAttachmentStatus.NORMAL)
                .build());

        // -------- Growth Logs (대표종 차트 데이터 포함) --------
        // 1. 대표종(감태) 데이터 - 차트에 표시됨
        area.addGrowth(GrowthLog.builder()
                .species(gamtae)
                .recordDate(LocalDate.of(2025, 5, 15))
                .attachmentRate(85.0)
                .survivalRate(90.0)
                .growthLength(12.5)
                .status(SpeciesAttachmentStatus.GOOD)
                .build());

        area.addGrowth(GrowthLog.builder()
                .species(gamtae)
                .recordDate(LocalDate.of(2025, 6, 15))
                .attachmentRate(88.0)
                .survivalRate(89.0)
                .growthLength(18.2)
                .status(SpeciesAttachmentStatus.GOOD)
                .build());

        // 2. 비대표종(다시마) 데이터 - 차트에 표시 안 됨 (필터링 테스트용)
        area.addGrowth(GrowthLog.builder()
                .species(dasima)
                .recordDate(LocalDate.of(2025, 6, 15))
                .attachmentRate(70.0)
                .survivalRate(75.0)
                .growthLength(10.0)
                .status(SpeciesAttachmentStatus.NORMAL)
                .build());

        // -------- Water Logs --------
        area.addWater(WaterLog.builder()
                .recordDate(LocalDate.of(2025, 6, 1))
                .temperature(18.2)
                .visibility(MarineStatus.GOOD)
                .current(MarineStatus.NORMAL)
                .surge(MarineStatus.NORMAL)
                .wave(MarineStatus.GOOD)
                .build());

        area.addWater(WaterLog.builder()
                .recordDate(LocalDate.of(2025, 7, 1))
                .temperature(21.5)
                .visibility(MarineStatus.NORMAL)
                .current(MarineStatus.NORMAL)
                .surge(MarineStatus.POOR)
                .wave(MarineStatus.NORMAL)
                .build());

        // -------- Media Logs --------
        area.addMedia(MediaLog.builder()
                .category(MediaCategory.BEFORE)
                .mediaUrl(VALID_IMAGE_URL)
                .recordDate(LocalDate.of(2025, 2, 20))
                .caption("복원 전 수중 상태")
                .build());

        area.addMedia(MediaLog.builder()
                .category(MediaCategory.AFTER)
                .mediaUrl(VALID_IMAGE_URL)
                .recordDate(LocalDate.of(2025, 4, 10))
                .caption("1차 이식 완료 후")
                .build());

        area.addMedia(MediaLog.builder()
                .category(MediaCategory.TIMELINE)
                .mediaUrl(VALID_IMAGE_URL)
                .recordDate(LocalDate.of(2025, 6, 15))
                .caption("6월 성장 관측")
                .build());

        projectAreaRepository.save(area);
    }

    /**
     * 울진 테스트 영역 1
     */
    private void createUljinDemoArea1(Species gamtae, Species dasima) {
        ProjectArea area = ProjectArea.builder()
                .name("울진 테스트 영역-1")
                .restorationRegion(RestorationRegion.ULJIN)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(null)
                .habitat(HabitatType.MIXED)
                .depth(8.0)
                .areaSize(920.0)
                .level(ProjectLevel.SETTLEMENT)
                .attachmentStatus(AreaAttachmentStatus.DECREASED)
                .build();

        area.setLocation(36.9950, 129.4020);

        // [설정] 울진 영역 대표종: 감태
        area.setRepresentativeSpecies(gamtae);

        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 8, 5))
                .species(gamtae)
                .method(TransplantMethod.ROCK_FIXATION)
                .count(30)
                .areaSize(180.0)
                .attachmentStatus(SpeciesAttachmentStatus.NORMAL)
                .build());

        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 9, 2))
                .species(dasima)
                .method(TransplantMethod.DIRECT_FIXATION)
                .count(15)
                .areaSize(90.0)
                .attachmentStatus(SpeciesAttachmentStatus.POOR)
                .build());

        // [추가] 대표종(감태) 성장 로그 데이터 추가 (차트 표시용)
        area.addGrowth(GrowthLog.builder()
                .species(gamtae)
                .recordDate(LocalDate.of(2025, 8, 20))
                .attachmentRate(75.0)
                .survivalRate(85.0)
                .growthLength(5.5)
                .status(SpeciesAttachmentStatus.NORMAL)
                .build());

        area.addGrowth(GrowthLog.builder()
                .species(gamtae)
                .recordDate(LocalDate.of(2025, 9, 20))
                .attachmentRate(78.0)
                .survivalRate(82.0)
                .growthLength(8.2)
                .status(SpeciesAttachmentStatus.GOOD)
                .build());

        area.addWater(WaterLog.builder()
                .recordDate(LocalDate.of(2025, 9, 1))
                .temperature(20.0)
                .visibility(MarineStatus.NORMAL)
                .current(MarineStatus.GOOD)
                .surge(MarineStatus.NORMAL)
                .wave(MarineStatus.NORMAL)
                .build());

        area.addMedia(MediaLog.builder()
                .category(MediaCategory.TIMELINE)
                .mediaUrl(VALID_IMAGE_URL)
                .recordDate(LocalDate.of(2025, 9, 1))
                .caption("울진 9월 관측")
                .build());

        projectAreaRepository.save(area);
    }
}