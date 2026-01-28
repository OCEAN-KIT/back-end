package com.ocean.piuda.dashboard.initializer;

import com.ocean.piuda.bio.entity.Species;
import com.ocean.piuda.bio.enums.BioGroup;
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
        Species gamtae = getOrCreateSpecies("감태", BioGroup.MACROALGAE);
        Species dasima = getOrCreateSpecies("다시마", BioGroup.MACROALGAE);
        Species mojaban = getOrCreateSpecies("모자반", BioGroup.MACROALGAE);

        // 2) Areas
        createPohangDemoArea1(gamtae, dasima, mojaban);
        createUljinDemoArea1(gamtae, dasima);

        log.info("OC DASHBOARD 데이터 초기화 완료");
    }

    private Species getOrCreateSpecies(String name, BioGroup category) {
        return speciesRepository.findByName(name)
                .orElseGet(() -> speciesRepository.save(
                        Species.builder()
                                .name(name)
                                .category(category)
                                .build()
                ));
    }

    /**
     * 포항 테스트 영역 1: 이식 + 성장(대표개체) + 환경 + 미디어 데이터 모두 포함
     * - TransplantLog: recordDate, attachmentStatus 필수
     * - GrowthLog: recordDate/attachmentRate/survivalRate/growthLength/status 필수
     * - WaterLog: recordDate/temperature/dissolvedOxygen/nutrient + MarineStatus 4종 필수
     * - MediaLog: recordDate/mediaUrl/category 필수
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

        // setLocation(lat, lon) (내부에서 Point(lon, lat) 생성)
        area.setLocation(36.0762, 129.4432);

        // -------- Transplant Logs (필수값: recordDate, attachmentStatus 포함) --------
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

        // 같은 method의 최신 착생상태 쿼리 검증용으로 하나 더(날짜 최신)
        area.addTransplant(TransplantLog.builder()
                .recordDate(LocalDate.of(2025, 6, 20))
                .species(mojaban)
                .method(TransplantMethod.ROPE)
                .count(8)
                .areaSize(120.0)
                .attachmentStatus(SpeciesAttachmentStatus.NORMAL)
                .build());

        // -------- Growth Logs (대표개체, 필수값 모두 포함) --------
        area.addGrowth(GrowthLog.builder()
                .species(gamtae)
                .isRepresentative(true)
                .recordDate(LocalDate.of(2025, 5, 15))
                .attachmentRate(85.0)
                .survivalRate(90.0)
                .growthLength(12.5)
                .status(SpeciesAttachmentStatus.GOOD)
                .build());

        area.addGrowth(GrowthLog.builder()
                .species(gamtae)
                .isRepresentative(true)
                .recordDate(LocalDate.of(2025, 6, 15))
                .attachmentRate(88.0)
                .survivalRate(89.0)
                .growthLength(18.2)
                .status(SpeciesAttachmentStatus.GOOD)
                .build());

        // 대표개체가 아닌 로그도 1개 추가(대표 차트 필터링 확인용)
        area.addGrowth(GrowthLog.builder()
                .species(dasima)
                .isRepresentative(false)
                .recordDate(LocalDate.of(2025, 6, 15))
                .attachmentRate(70.0)
                .survivalRate(75.0)
                .growthLength(10.0)
                .status(SpeciesAttachmentStatus.NORMAL)
                .build());

        // -------- Water Logs (필수값: recordDate, temperature, DO, nutrient + 상태값 4종) --------
        area.addWater(WaterLog.builder()
                .recordDate(LocalDate.of(2025, 6, 1))
                .temperature(18.2)
                .dissolvedOxygen(7.4)
                .nutrient(0.35)
                .visibility(MarineStatus.GOOD)
                .current(MarineStatus.NORMAL)
                .surge(MarineStatus.NORMAL)
                .wave(MarineStatus.GOOD)
                .build());

        area.addWater(WaterLog.builder()
                .recordDate(LocalDate.of(2025, 7, 1))
                .temperature(21.5)
                .dissolvedOxygen(6.9)
                .nutrient(0.42)
                .visibility(MarineStatus.NORMAL)
                .current(MarineStatus.NORMAL)
                .surge(MarineStatus.POOR)
                .wave(MarineStatus.NORMAL)
                .build());

        // -------- Media Logs (필수값: recordDate, mediaUrl, category) --------
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
     * 울진 테스트 영역 1: 마커/통계/nearest/bbox 테스트용으로 최소 구성 + 필수로그 몇 개
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

        area.addWater(WaterLog.builder()
                .recordDate(LocalDate.of(2025, 9, 1))
                .temperature(20.0)
                .dissolvedOxygen(7.0)
                .nutrient(0.40)
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
