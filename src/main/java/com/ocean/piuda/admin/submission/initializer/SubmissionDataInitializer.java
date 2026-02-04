package com.ocean.piuda.admin.submission.initializer;

import com.ocean.piuda.admin.common.enums.*;
import com.ocean.piuda.admin.site.entity.SiteNameOption;
import com.ocean.piuda.admin.site.repository.SiteNameOptionRepository;
import com.ocean.piuda.admin.submission.entity.*;
import com.ocean.piuda.admin.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않음
@Order(1)
public class SubmissionDataInitializer implements CommandLineRunner {

    private final SubmissionRepository submissionRepository;
    private final SiteNameOptionRepository siteNameOptionRepository;

    // 이미지 URL 상수
    private static final String VALID_IMAGE_URL = "/images/underSea.jpg";

    @Override
    @Transactional
    public void run(String... args) {
        if (submissionRepository.count() > 0) {
            log.info("Submission 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("Admin Submission 테스트 데이터 초기화 시작...");

        // 0. SiteNameOption 마스터 데이터 생성
        SiteNameOption optionCrossReef = siteNameOptionRepository.save(SiteNameOption.builder().name("십자형 어초").isActive(true).build());
        SiteNameOption optionCage1 = siteNameOptionRepository.save(SiteNameOption.builder().name("가두리 #1").isActive(true).build());
        SiteNameOption optionCage2 = siteNameOptionRepository.save(SiteNameOption.builder().name("가두리 #2").isActive(true).build());

        List<Submission> submissions = new ArrayList<>();

        // 1. 검수 대기 (이식) - 옵션 선택(십자형 어초)
        submissions.add(createSubmission(
                1L,
                "십자형 어초", // 스냅샷 이름
                optionCrossReef, // 선택된 옵션 엔티티
                ActivityType.TRANSPLANT, SubmissionStatus.SUBMITTED,
                "홍길동", "hong@example.com", 3,
                "깨끗해진 바다가 뿌듯합니다",
                new BigDecimal("36.0322"), new BigDecimal("129.3650"),
                LocalDate.now().minusDays(2), LocalTime.of(9, 0),
                18.5,  // 수온
                MarineCondition.GOOD,   // 시야
                10.5,  // 수심
                MarineCondition.NORMAL, // 파도
                MarineCondition.NORMAL, // 서지
                MarineCondition.NORMAL, // 조류
                "홍길동, 김철수",
                "이식 작업을 수행했습니다. 총 50개를 이식했습니다.",
                "50개"
        ));

        // 2. 승인 (해양정화) - 직접 입력(부산 송도해수욕장, 옵션 Null)
        submissions.add(createSubmission(
                2L,
                "부산 송도해수욕장",
                null, // 직접 입력
                ActivityType.TRASH_COLLECTION, SubmissionStatus.APPROVED,
                "김철수", "kim@example.com", 2,
                "해변 청소 활동에 참여했습니다",
                new BigDecimal("35.0784"), new BigDecimal("129.0756"),
                LocalDate.now().minusDays(5), LocalTime.of(10, 0),
                20.0,
                MarineCondition.GOOD,
                5.0,
                MarineCondition.GOOD,   // 파도
                MarineCondition.GOOD,   // 서지
                MarineCondition.GOOD,   // 조류
                "김철수, 이영희, 박민수",
                "플라스틱 병 30개, 캔 15개를 수거했습니다.",
                "45kg"
        ));

        // 3. 반려 (기타) - 옵션 선택(가두리 #1)
        Submission rejectedSubmission = createSubmission(
                3L,
                "가두리 #1",
                optionCage1,
                ActivityType.OTHER, SubmissionStatus.REJECTED,
                "이영희", "lee@example.com", 1,
                "제주도 바다 정화 활동",
                new BigDecimal("33.4584"), new BigDecimal("126.9426"),
                LocalDate.now().minusDays(7), LocalTime.of(8, 0),
                22.0,
                MarineCondition.NORMAL,
                8.0,
                MarineCondition.GOOD,
                MarineCondition.GOOD,
                MarineCondition.GOOD,
                "이영희",
                "해양 생물 관찰 및 정화 활동",
                null
        );
        RejectReason rejectReason = RejectReason.builder()
                .submission(rejectedSubmission)
                .templateCode("PHOTO_INSUFFICIENT")
                .message("사진이 부족합니다. 최소 3장 이상의 사진을 첨부해주세요.")
                .rejectedBy("admin@oceancampus.kr")
                .rejectedAt(LocalDateTime.now().minusDays(6))
                .build();
        rejectedSubmission.setRejectReason(rejectReason);
        submissions.add(rejectedSubmission);

        // 4. 검수 대기 (이식) - 직접 입력
        submissions.add(createSubmission(
                4L,
                "강원도 속초 해변",
                null,
                ActivityType.TRANSPLANT, SubmissionStatus.SUBMITTED,
                "박민수", "park@example.com", 4,
                "속초 바다 정화 활동에 참여했습니다",
                new BigDecimal("38.2070"), new BigDecimal("128.5918"),
                LocalDate.now().minusDays(1), LocalTime.of(13, 0),
                16.0,
                MarineCondition.BAD,
                15.0,
                MarineCondition.BAD,    // 파도 높음
                MarineCondition.BAD,    // 서지 있음
                MarineCondition.BAD,    // 조류 강함
                "박민수, 최지영",
                "이식 80개 완료",
                "80개"
        ));

        // 5. 승인 (해양정화) - 옵션 선택(가두리 #2)
        submissions.add(createSubmission(
                5L,
                "가두리 #2",
                optionCage2,
                ActivityType.TRASH_COLLECTION, SubmissionStatus.APPROVED,
                "최지영", "choi@example.com", 5,
                "여수 바다가 아름답습니다",
                new BigDecimal("34.7604"), new BigDecimal("127.6622"),
                LocalDate.now().minusDays(10), LocalTime.of(9, 30),
                19.0,
                MarineCondition.GOOD,
                7.0,
                MarineCondition.NORMAL,
                MarineCondition.NORMAL,
                MarineCondition.NORMAL,
                "최지영, 홍길동",
                "쓰레기 100개 수거 완료",
                "100kg"
        ));

        submissionRepository.saveAll(submissions);
        log.info("{}개의 Submission 테스트 데이터 생성 완료", submissions.size());
    }

    private Submission createSubmission(
            Long id,
            String siteName,
            SiteNameOption siteNameOption, // [추가] 옵션 엔티티
            ActivityType activityType,
            SubmissionStatus status,
            String authorName,
            String authorEmail,
            int attachmentCount,
            String workDescription,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDate recordDate,
            LocalTime startTime,
            Double waterTempC,
            MarineCondition visibilityStatus,
            Double depthM,
            MarineCondition waveStatus,
            MarineCondition surgeStatus,
            MarineCondition currentStatus,
            String participantNames,
            String activityDetails,
            String amountOrScale
    ) {
        LocalDateTime submittedAt = LocalDateTime.of(recordDate, startTime);

        // 1. BasicEnv 생성
        BasicEnv basicEnv = BasicEnv.builder()
                .recordDate(recordDate)
                .avgDepthM(depthM)
                .maxDepthM(depthM + 2.0)
                .waterTempC(waterTempC)
                .visibilityStatus(visibilityStatus)
                .waveStatus(waveStatus)
                .surgeStatus(surgeStatus)
                .currentStatus(currentStatus)
                .build();

        // 2. Submission 생성 (StructureType 제거, SiteNameOption 추가)
        Submission submission = Submission.builder()
                .siteName(siteName)             // 스냅샷
                .siteNameOption(siteNameOption) // 연관관계 (Nullable)
                .recordDate(recordDate)
                .divingRound(1)
                .activityType(activityType)
                .status(status)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .attachmentCount(attachmentCount)
                .workDescription(workDescription)
                .latitude(latitude)
                .longitude(longitude)
                .participantNames(participantNames)
                .submittedAt(submittedAt)
                .build();

        // 3. 관계 설정 (Env)
        submission.setBasicEnv(basicEnv);

        // 4. Activity Type별 상세 엔티티 생성 및 연결
        createSpecificActivity(submission, activityType, activityDetails, amountOrScale);

        // 5. Attachments 생성
        for (int i = 1; i <= attachmentCount; i++) {
            Attachment attachment = Attachment.builder()
                    .fileName("photo" + i + ".jpg")
                    .fileUrl(VALID_IMAGE_URL)
                    .mimeType("image/jpeg")
                    .fileSize(1024 * 500 * i)
                    .uploadedAt(submittedAt)
                    .build();
            submission.addAttachment(attachment);
        }

        // 6. AuditLog 생성 (제출됨)
        AuditLog submitLog = AuditLog.builder()
                .submission(submission)
                .action(AuditAction.SUBMITTED)
                .performedBy("system")
                .comment(null)
                .createdAt(submittedAt)
                .build();
        submission.addAuditLog(submitLog);

        if (status == SubmissionStatus.APPROVED) {
            AuditLog approveLog = AuditLog.builder()
                    .submission(submission)
                    .action(AuditAction.APPROVED)
                    .performedBy("admin@oceancampus.kr")
                    .comment(null)
                    .createdAt(submittedAt.plusDays(1))
                    .build();
            submission.addAuditLog(approveLog);
        } else if (status == SubmissionStatus.REJECTED) {
            AuditLog rejectLog = AuditLog.builder()
                    .submission(submission)
                    .action(AuditAction.REJECTED)
                    .performedBy("admin@oceancampus.kr")
                    .comment("사진이 부족합니다.")
                    .createdAt(submittedAt.plusDays(1))
                    .build();
            submission.addAuditLog(rejectLog);
        }

        return submission;
    }

    private void createSpecificActivity(Submission submission, ActivityType type, String details, String amount) {
        switch (type) {
            case TRANSPLANT:
                ActivityTransplant transplant = ActivityTransplant.builder()
                        .submission(submission)
                        .speciesType(TransplantSpeciesType.KAMTAE)
                        .locationType(TransplantLocationType.REEF)
                        .methodType(TransplantMethodType.ROPE_LINE)
                        .scale(amount != null ? amount : "규모 미상")
                        .healthStatus(HealthStatus.A)
                        .build();
                submission.setActivityTransplant(transplant);
                break;

            case TRASH_COLLECTION:
            case MARINE_CLEANUP:
                ActivityMarineCleanup cleanup = ActivityMarineCleanup.builder()
                        .submission(submission)
                        .wasteTypes(List.of(WasteType.PLASTIC, WasteType.NET))
                        .method(CleanupMethodType.HAND)
                        .collectionAmount(amount != null ? amount : "0kg")
                        .uncollectedScale(UncollectedScale.SMALL)
                        .build();
                submission.setActivityMarineCleanup(cleanup);
                break;

            case GRAZER_REMOVAL:
                ActivityGrazerRemoval grazer = ActivityGrazerRemoval.builder()
                        .submission(submission)
                        .targetSpecies(List.of(GrazerSpeciesType.URCHIN))
                        .densityBeforeWork(DensityLevel.HIGH)
                        .workScope(WorkScope.ZONE)
                        .collectionAmount(amount != null ? amount : "0kg")
                        .note(details)
                        .build();
                submission.setActivityGrazerRemoval(grazer);
                break;

            case MONITORING:
                ActivityMonitoring monitoring = ActivityMonitoring.builder()
                        .submission(submission)
                        .terrain(TerrainType.ROCK)
                        .barrenExtent(BarrenExtent.ONGOING)
                        .grazerDistribution(DensityLevel.MID)
                        .rockFeatures(List.of(RockFeature.SMOOTH))
                        .suitability(Suitability.SUITABLE)
                        .seaweedHealthStatus(SeaweedHealthStatus.GOOD)
                        .build();
                submission.setActivityMonitoring(monitoring);
                break;

            case SUBSTRATE_IMPROVEMENT:
                ActivitySubstrateImprovement substrate = ActivitySubstrateImprovement.builder()
                        .submission(submission)
                        .targetType(SubstrateTargetType.ROCK)
                        .workScope(amount != null ? amount : "범위 미상")
                        .substrateState("양호")
                        .build();
                submission.setActivitySubstrateImprovement(substrate);
                break;

            case OTHER:
            default:
                break;
        }
    }
}