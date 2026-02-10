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

        Species gamtae = getOrCreateSpecies("감태");
        Species dasima = getOrCreateSpecies("다시마");
        Species mojaban = getOrCreateSpecies("모자반");

        createPohangDemoArea1(gamtae, dasima, mojaban); // 데이터 5개월치 (LIMIT 3 테스트)
        createUljinDemoArea1(gamtae, dasima);          // 데이터 2개월치 (데이터 부족 상황 테스트)

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
     * 포항 테스트 영역 1: 데이터가 존재하는 월이 총 5개 (3, 4, 6, 8, 10월)
     * WaterLog와 GrowthLog도 함께 생성하여 차트가 빈 칸으로 나오지 않게 함
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
        area.setRepresentativeSpecies(gamtae);

        // 1. Transplant Logs (최신 3개월 쿼리 검증용: 3, 4, 6, 8, 10월 데이터)
        area.addTransplant(createTransplant(gamtae, LocalDate.of(2025, 3, 10), TransplantMethod.SEEDLING_STRING));
        area.addTransplant(createTransplant(mojaban, LocalDate.of(2025, 4, 5), TransplantMethod.ROPE));
        area.addTransplant(createTransplant(mojaban, LocalDate.of(2025, 6, 20), TransplantMethod.ROPE));
        area.addTransplant(createTransplant(dasima, LocalDate.of(2025, 8, 15), TransplantMethod.TRANSPLANT_MODULE));
        area.addTransplant(createTransplant(gamtae, LocalDate.of(2025, 10, 22), TransplantMethod.SEEDLING_STRING));

        // 2. Water Logs (데이터 없음 방지 - 용존산소 제거됨)
        area.addWater(createWater(LocalDate.of(2025, 6, 1), 18.2));
        area.addWater(createWater(LocalDate.of(2025, 10, 1), 21.5));

        // 3. Growth Logs (대표종 차트 데이터용)
        area.addGrowth(createGrowth(gamtae, LocalDate.of(2025, 6, 15), 12.5));
        area.addGrowth(createGrowth(gamtae, LocalDate.of(2025, 10, 15), 22.0));

        // 4. Media Logs
        area.addMedia(createMedia(MediaCategory.BEFORE, LocalDate.of(2025, 2, 20), "복원 전 수중 상태"));
        area.addMedia(createMedia(MediaCategory.AFTER, LocalDate.of(2025, 4, 10), "1차 이식 완료"));
        area.addMedia(createMedia(MediaCategory.TIMELINE, LocalDate.of(2025, 10, 15), "10월 성장 관측"));

        projectAreaRepository.save(area);
    }

    private TransplantLog createTransplant(Species s, LocalDate d, TransplantMethod m) {
        return TransplantLog.builder()
                .recordDate(d).species(s).method(m)
                .count(15).areaSize(150.0).attachmentStatus(SpeciesAttachmentStatus.NORMAL).build();
    }

    private WaterLog createWater(LocalDate d, double temp) {
        return WaterLog.builder()
                .recordDate(d).temperature(temp)
                .visibility(MarineStatus.GOOD).current(MarineStatus.NORMAL)
                .surge(MarineStatus.NORMAL).wave(MarineStatus.GOOD).build();
    }

    private GrowthLog createGrowth(Species s, LocalDate d, double len) {
        return GrowthLog.builder()
                .species(s).recordDate(d).growthLength(len)
                .status(SpeciesAttachmentStatus.GOOD).build();
    }

    private MediaLog createMedia(MediaCategory c, LocalDate d, String cap) {
        return MediaLog.builder()
                .category(c).mediaUrl(VALID_IMAGE_URL).recordDate(d).caption(cap).build();
    }

    /**
     * 울진 테스트 영역 1: 데이터가 존재하는 월이 총 2개 (8, 9월)
     * -> 데이터가 3개 미만이어도 오류 없이 2개 모두 차트에 나와야 함
     */
    private void createUljinDemoArea1(Species gamtae, Species dasima) {
        ProjectArea area = ProjectArea.builder()
                .name("울진 테스트 영역-1")
                .restorationRegion(RestorationRegion.ULJIN)
                .startDate(LocalDate.of(2025, 8, 1))
                .habitat(HabitatType.MIXED)
                .depth(8.0)
                .areaSize(920.0)
                .level(ProjectLevel.SETTLEMENT)
                .attachmentStatus(AreaAttachmentStatus.DECREASED)
                .build();

        area.setLocation(36.9950, 129.4020);
        area.setRepresentativeSpecies(gamtae);

        // 1. Transplant Logs (8, 9월)
        area.addTransplant(createTransplant(gamtae, LocalDate.of(2025, 8, 5), TransplantMethod.ROCK_FIXATION));
        area.addTransplant(createTransplant(dasima, LocalDate.of(2025, 9, 2), TransplantMethod.DIRECT_FIXATION));

        // 2. Water Logs
        area.addWater(createWater(LocalDate.of(2025, 8, 1), 22.1));
        area.addWater(createWater(LocalDate.of(2025, 9, 1), 20.4));

        // 3. Growth Logs
        area.addGrowth(createGrowth(gamtae, LocalDate.of(2025, 8, 20), 5.5));
        area.addGrowth(createGrowth(gamtae, LocalDate.of(2025, 9, 20), 8.8));

        // 4. Media Logs
        area.addMedia(createMedia(MediaCategory.BEFORE, LocalDate.of(2025, 7, 25), "울진 복원 전"));
        area.addMedia(createMedia(MediaCategory.TIMELINE, LocalDate.of(2025, 9, 20), "9월 진행 상태"));

        projectAreaRepository.save(area);
    }
}