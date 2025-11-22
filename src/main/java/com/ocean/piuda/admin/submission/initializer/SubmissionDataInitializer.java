package com.ocean.piuda.admin.submission.initializer;

import com.ocean.piuda.admin.common.enums.*;
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

    @Override
    @Transactional
    public void run(String... args) {
        if (submissionRepository.count() > 0) {
            log.info("Submission 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("Admin Submission 테스트 데이터 초기화 시작...");

        List<Submission> submissions = new ArrayList<>();

        // 1. 검수 대기 중인 제출 (PENDING)
        submissions.add(createSubmission(
                1L,
                "포항 해안가",
                ActivityType.TRANSPLANT,
                SubmissionStatus.PENDING,
                "홍길동",
                "hong@example.com",
                3,
                "깨끗해진 바다가 뿌듯합니다",
                new BigDecimal("36.0322"),
                new BigDecimal("129.3650"),
                LocalDate.now().minusDays(2),
                LocalTime.of(9, 0),
                LocalTime.of(12, 30),
                18.5f,
                15.0f,
                10.5f,
                CurrentState.MEDIUM,
                Weather.SUNNY,
                "홍길동",
                5,
                ParticipantRole.CITIZEN_DIVER,
                "이식 작업을 수행했습니다. 총 50개를 이식했습니다.",
                50.0f,
                3.5f,
                "public/user_objects/2025-01-15/photo1.jpg"
        ));

        // 2. 승인된 제출 (APPROVED)
        submissions.add(createSubmission(
                2L,
                "부산 송도해수욕장",
                ActivityType.TRASH_COLLECTION,
                SubmissionStatus.APPROVED,
                "김철수",
                "kim@example.com",
                2,
                "해변 청소 활동에 참여했습니다",
                new BigDecimal("35.0784"),
                new BigDecimal("129.0756"),
                LocalDate.now().minusDays(5),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                20.0f,
                20.0f,
                5.0f,
                CurrentState.LOW,
                Weather.SUNNY,
                "김철수",
                3,
                ParticipantRole.CITIZEN_DIVER,
                "플라스틱 병 30개, 캔 15개를 수거했습니다.",
                45.0f,
                4.0f,
                "public/user_objects/2025-01-10/photo2.jpg"
        ));

        // 3. 반려된 제출 (REJECTED)
        Submission rejectedSubmission = createSubmission(
                3L,
                "제주도 성산일출봉",
                ActivityType.OTHER,
                SubmissionStatus.REJECTED,
                "이영희",
                "lee@example.com",
                1,
                "제주도 바다 정화 활동",
                new BigDecimal("33.4584"),
                new BigDecimal("126.9426"),
                LocalDate.now().minusDays(7),
                LocalTime.of(8, 0),
                LocalTime.of(11, 0),
                22.0f,
                25.0f,
                8.0f,
                CurrentState.LOW,
                Weather.CLOUDY,
                "이영희",
                2,
                ParticipantRole.RESEARCHER,
                "해양 생물 관찰 및 정화 활동",
                10.0f,
                3.0f,
                "public/user_objects/2025-01-08/photo3.jpg"
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

        // 4. 검수 대기 중인 제출 (PENDING) - 추가
        submissions.add(createSubmission(
                4L,
                "강원도 속초 해변",
                ActivityType.TRANSPLANT,
                SubmissionStatus.PENDING,
                "박민수",
                "park@example.com",
                4,
                "속초 바다 정화 활동에 참여했습니다",
                new BigDecimal("38.2070"),
                new BigDecimal("128.5918"),
                LocalDate.now().minusDays(1),
                LocalTime.of(13, 0),
                LocalTime.of(17, 0),
                16.0f,
                12.0f,
                15.0f,
                CurrentState.HIGH,
                Weather.WINDY,
                "박민수",
                4,
                ParticipantRole.LOCAL_MANAGER,
                "이식 80개 완료",
                80.0f,
                4.0f,
                "public/user_objects/2025-01-16/photo4.jpg"
        ));

        // 5. 승인된 제출 (APPROVED) - 추가
        submissions.add(createSubmission(
                5L,
                "전라남도 여수 해수욕장",
                ActivityType.TRASH_COLLECTION,
                SubmissionStatus.APPROVED,
                "최지영",
                "choi@example.com",
                5,
                "여수 바다가 아름답습니다",
                new BigDecimal("34.7604"),
                new BigDecimal("127.6622"),
                LocalDate.now().minusDays(10),
                LocalTime.of(9, 30),
                LocalTime.of(13, 30),
                19.0f,
                18.0f,
                7.0f,
                CurrentState.MEDIUM,
                Weather.SUNNY,
                "최지영",
                6,
                ParticipantRole.CITIZEN_DIVER,
                "쓰레기 100개 수거 완료",
                100.0f,
                4.0f,
                "public/user_objects/2025-01-07/photo5.jpg"
        ));

        submissionRepository.saveAll(submissions);
        log.info("{}개의 Submission 테스트 데이터 생성 완료", submissions.size());
    }

    private Submission createSubmission(
            Long id,
            String siteName,
            ActivityType activityType,
            SubmissionStatus status,
            String authorName,
            String authorEmail,
            int attachmentCount,
            String feedbackText,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDate recordDate,
            LocalTime startTime,
            LocalTime endTime,
            Float waterTempC,
            Float visibilityM,
            Float depthM,
            CurrentState currentState,
            Weather weather,
            String leaderName,
            int participantCount,
            ParticipantRole role,
            String activityDetails,
            Float collectionAmount,
            Float durationHours,
            String fileUrl
    ) {
        LocalDateTime submittedAt = LocalDateTime.of(recordDate, startTime);

        // BasicEnv 생성
        BasicEnv basicEnv = BasicEnv.builder()
                .recordDate(recordDate)
                .startTime(startTime)
                .endTime(endTime)
                .waterTempC(waterTempC)
                .visibilityM(visibilityM)
                .depthM(depthM)
                .currentState(currentState)
                .weather(weather)
                .build();

        // Participants 생성
        Participants participants = Participants.builder()
                .leaderName(leaderName)
                .participantCount(participantCount)
                .role(role)
                .build();

        // Activity 생성
        Activity activity = Activity.builder()
                .type(activityType)
                .details(activityDetails)
                .collectionAmount(collectionAmount)
                .durationHours(durationHours)
                .build();

        // Attachments 생성
        List<Attachment> attachments = new ArrayList<>();
        String baseUrl = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
        for (int i = 1; i <= attachmentCount; i++) {
            Attachment attachment = Attachment.builder()
                    .fileName("photo" + i + ".jpg")
                    .fileUrl(baseUrl + "photo" + i + ".jpg")
                    .mimeType("image/jpeg")
                    .fileSize(1024 * 500 * i) // 500KB * i
                    .uploadedAt(submittedAt)
                    .build();
            attachments.add(attachment);
        }

        // Submission 생성
        Submission submission = Submission.builder()
                .siteName(siteName)
                .activityType(activityType)
                .submittedAt(submittedAt)
                .status(status)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .attachmentCount(attachmentCount)
                .feedbackText(feedbackText)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        // 관계 설정
        submission.setBasicEnv(basicEnv);
        submission.setParticipants(participants);
        submission.setActivity(activity);
        attachments.forEach(submission::addAttachment);

        // AuditLog 생성 (제출됨)
        AuditLog submitLog = AuditLog.builder()
                .submission(submission)
                .action(AuditAction.SUBMITTED)
                .performedBy("system")
                .comment(null)
                .createdAt(submittedAt)
                .build();
        submission.addAuditLog(submitLog);

        // 승인/반려된 경우 AuditLog 추가
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
                    .comment("사진이 부족합니다. 최소 3장 이상의 사진을 첨부해주세요.")
                    .createdAt(submittedAt.plusDays(1))
                    .build();
            submission.addAuditLog(rejectLog);
        }

        return submission;
    }
}
