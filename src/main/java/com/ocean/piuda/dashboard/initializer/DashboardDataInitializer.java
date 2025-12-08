package com.ocean.piuda.dashboard.initializer;

import com.ocean.piuda.dashboard.entity.*;
import com.ocean.piuda.dashboard.enums.HabitatType;
import com.ocean.piuda.dashboard.enums.ProjectStatus;
import com.ocean.piuda.dashboard.repository.ProjectAreaRepository;
import com.ocean.piuda.global.util.GeometryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DashboardDataInitializer implements CommandLineRunner {

    private final ProjectAreaRepository projectAreaRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (projectAreaRepository.count() > 0) {
            return;
        }

        log.info("Dashboard 초기 데이터 적재 시작");
        createPohangData();
    }

    private void createPohangData() {
        // 좌표 정의
        double lat = 36.0762;
        double lon = 129.4432;

        ProjectArea area = ProjectArea.builder()
                .name("작업 영역 1")
                .startDate(LocalDate.of(2025, 7, 15))
                .habitat(HabitatType.ROCKY_REEF)
                .depth(8.0)
                .areaSize(2100.0)
                .status(ProjectStatus.TRANSPLANT_COMPLETED)
                // [수정] Point 객체 생성 및 주입 (Lon, Lat 순서)
                .location(GeometryUtils.createPoint(lon, lat))
                .build();

        // 이식 데이터
        area.addTransplant(TransplantLog.builder().speciesName("감태").count(2100).areaSize(1100.0).build());
        area.addTransplant(TransplantLog.builder().speciesName("다시마").count(700).areaSize(650.0).build());
        area.addTransplant(TransplantLog.builder().speciesName("곰피").count(380).areaSize(350.0).build());

        // 시계열 데이터
        LocalDate baseDate = LocalDate.of(2025, 1, 15);
        List<Double> attach = List.of(60.0, 65.0, 71.0, 77.0, 80.0, 82.0);
        List<Double> surviv = List.of(92.0, 90.0, 89.0, 88.0, 86.0, 85.0);
        List<Double> growth = List.of(1.0, 1.2, 1.5, 1.8, 2.0, 2.3);

        List<Double> wTemp = List.of(9.6, 10.0, 11.3, 13.7, 16.1);
        List<Double> wDo = List.of(8.6, 8.4, 8.2, 8.0, 7.8);
        List<Double> wNut = List.of(0.29, 0.26, 0.24, 0.22, 0.21);

        for (int i = 0; i < 6; i++) {
            LocalDate date = baseDate.plusMonths(i);
            area.addGrowth(GrowthLog.builder()
                    .recordDate(date)
                    .attachmentRate(attach.get(i))
                    .survivalRate(surviv.get(i))
                    .growthLength(growth.get(i))
                    .build());

            if (i < 5) {
                area.addWater(WaterLog.builder()
                        .recordDate(date)
                        .temperature(wTemp.get(i))
                        .dissolvedOxygen(wDo.get(i))
                        .nutrient(wNut.get(i))
                        .build());
            }
        }

        area.setBiodiversity(BiodiversitySummary.builder()
                .fishCountBefore(5).fishCountAfter(11)
                .invertCountBefore(10).invertCountAfter(18)
                .shannonIndexBefore(1.12).shannonIndexAfter(1.78)
                .build());

        String imgUrl = "/images/underSea.jpg";
        area.addMedia(MediaLog.builder().recordDate(LocalDate.of(2025, 7, 1)).mediaUrl(imgUrl).caption("2025.07").build());
        area.addMedia(MediaLog.builder().recordDate(LocalDate.of(2025, 8, 1)).mediaUrl(imgUrl).caption("2025.08").build());

        projectAreaRepository.save(area);
        log.info("Pohang 데이터 생성 완료 (ID: {})", area.getId());
    }
}