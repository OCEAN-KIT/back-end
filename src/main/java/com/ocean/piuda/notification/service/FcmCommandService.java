package com.ocean.piuda.notification.service;

import com.google.firebase.messaging.*;
import com.ocean.piuda.notification.dto.request.SaveTokenRequest;
import com.ocean.piuda.notification.dto.request.SendNotificationRequest;
import com.ocean.piuda.notification.dto.response.SendResultResponse;
import com.ocean.piuda.notification.entity.FcmToken;
import com.ocean.piuda.notification.repository.FcmTokenRepository;
import com.ocean.piuda.user.entity.User;
import com.ocean.piuda.user.repository.UserRepository;
import com.ocean.piuda.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmCommandService {

    private static final int FCM_MULTICAST_LIMIT = 500;

    private final FirebaseMessaging messaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserQueryService userQueryService;

    public FcmToken upsert(Long userId, SaveTokenRequest req) {
        User user = userQueryService.getUserById(userId);
        FcmToken existing = fcmTokenRepository.findByToken(req.token()).orElse(null);
        if (existing != null) {
            // 소유자/클라이언트 정보 갱신만 허용
            existing.upsertUser(user);
            existing.updateClientInfo(req.deviceId(), req.platform());
            return existing;
        }
        return fcmTokenRepository.save(
                FcmToken.builder()
                        .user(user)
                        .token(req.token())
                        .deviceId(req.deviceId())
                        .platform(req.platform())
                        .build()
        );
    }

    public void removeByToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }


    /**
     * 단체/개인 알림 발송 처리
     */
    public SendResultResponse send(SendNotificationRequest req) throws FirebaseMessagingException {

        // 1) 대상 토큰 수집 (userIds + tokens 합집합)
        Set<String> targetTokens = new LinkedHashSet<>();



       if (req.userIds() != null && !req.userIds().isEmpty()) {
            // 유저별 모든 토큰 조회
            List<User> users = req.userIds().stream()
                    .map(userQueryService::getUserById)
                    .toList();

            users.forEach(u -> fcmTokenRepository.findAllByUser(u).forEach(t -> targetTokens.add(t.getToken())));
        }

        if (req.tokens() != null && !req.tokens().isEmpty()) targetTokens.addAll(req.tokens());


        if (targetTokens.isEmpty()) {
            return SendResultResponse.builder()
                    .successCount(0).failureCount(0).messageIds(List.of())
                    .build();
        }

        // 2) 500개씩 청크로 쪼개 전송
        int success = 0, failure = 0;
        List<String> messageIds = new ArrayList<>();

        for (List<String> chunk : partition(new ArrayList<>(targetTokens), FCM_MULTICAST_LIMIT)) {
            SendResultResponse res = sendToTokens(chunk, req.title(), req.body(), req.url());
            success += res.successCount();
            failure += res.failureCount();
            if (res.messageIds() != null) messageIds.addAll(res.messageIds());
        }

        return SendResultResponse.builder()
                .successCount(success)
                .failureCount(failure)
                .messageIds(messageIds)
                .build();
    }

    /**
     * 헬퍼
     */
    public SendResultResponse sendToUser(Long userId, String title, String body, String url) throws FirebaseMessagingException {
        User user = userQueryService.getUserById(userId);
        List<String> tokens = fcmTokenRepository.findAllByUser(user).stream().map(FcmToken::getToken).toList();
        return sendToTokens(tokens, title, body, url);
    }

    public SendResultResponse sendToTokens(List<String> tokens, String title, String body, String url) throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            return SendResultResponse.builder().successCount(0).failureCount(0).messageIds(List.of()).build();
        }

        MulticastMessage msg = MulticastMessage.builder()
                .putData("url", url == null ? "/" : url)
                .setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTitle(Objects.requireNonNullElse(title, "알림"))
                                .setBody(Objects.requireNonNullElse(body, ""))
                                .build())
                        .setFcmOptions(WebpushFcmOptions.withLink(url == null ? "/" : url))
                        .putHeader("TTL", String.valueOf(Duration.ofMinutes(5).toSeconds()))
                        .build())
                .addAllTokens(tokens)
                .build();

        BatchResponse batch = messaging.sendEachForMulticast(msg);

        // 실패 토큰 정리
        List<String> toDelete = new ArrayList<>();
        List<String> messageIds = new ArrayList<>();

        for (int i = 0; i < batch.getResponses().size(); i++) {
            SendResponse r = batch.getResponses().get(i);
            if (r.isSuccessful()) {
                if (r.getMessageId() != null) {
                    messageIds.add(r.getMessageId());
                }
            } else if (r.getException() instanceof FirebaseMessagingException fme) {
                MessagingErrorCode code = fme.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    toDelete.add(tokens.get(i));
                }
            }
        }
        toDelete.forEach(fcmTokenRepository::deleteByToken);

        // Multicast는 개별 messageId 수집을 보장하진 않으니 빈 리스트로 반환(필요시 추후 확장)
        return SendResultResponse.builder()
                .successCount(batch.getSuccessCount())
                .failureCount(batch.getFailureCount())
                .messageIds(messageIds)
                .build();
    }

    /**
     *유틸
     */
    private static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        int n = list.size();
        List<List<T>> parts = new ArrayList<>((n + size - 1) / size);
        for (int i = 0; i < n; i += size) {
            parts.add(list.subList(i, Math.min(n, i + size)));
        }
        return parts;
    }
}



