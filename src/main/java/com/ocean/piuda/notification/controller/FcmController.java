package com.ocean.piuda.notification.controller;

import com.ocean.piuda.global.api.dto.ApiData;
import com.ocean.piuda.notification.dto.request.SaveTokenRequest;
import com.ocean.piuda.notification.dto.request.SendNotificationRequest;
import com.ocean.piuda.notification.dto.response.FcmTokensResponse;
import com.ocean.piuda.notification.dto.response.SendResultResponse;
import com.ocean.piuda.notification.service.FcmCommandService;
import com.ocean.piuda.notification.service.FcmQueryService;
import com.ocean.piuda.security.oauth2.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Push/FCM API", description = "웹푸시(FCM) 관련 API")
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class FcmController {

    private final FcmCommandService fcmCommandService;
    private final FcmQueryService fcmQueryService;


    /** 토큰 저장/갱신 (로그인 유저 기준) */
    @PostMapping("/tokens")
    @Operation(
            summary = "FCM 토큰 저장/갱신",
            description = "현재 로그인한 사용자 기준으로 FCM/WebPush 토큰을 저장하거나 갱신합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<Boolean> upsertToken(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저장/갱신할 토큰 정보", required = true
            )
            @RequestBody SaveTokenRequest req
    ) {


            fcmCommandService.upsert(principalDetails.getUser().getId(), req);
            return ApiData.ok(true);

    }

    /** 토큰 삭제 */
    @DeleteMapping("/tokens/{token}")
    @Operation(
            summary = "FCM 토큰 삭제",
            description = "지정한 FCM/WebPush 토큰을 삭제합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<Boolean>  removeToken(
            @Parameter(description = "삭제할 FCM/WebPush 토큰 값", required = true)
            @PathVariable String token
    ) {
        fcmCommandService.removeByToken(token);
        return ApiData.ok(true);

    }

    /** (관리/테스트) 알림 발송 */
    @PostMapping("/send")
    @Operation(
            summary = "알림 발송(관리/테스트)",
            description = "특정 토큰 또는 사용자 대상으로 알림을 발송합니다. 운영 환경에서는 관리자 권한에서만 사용하십시오."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<SendResultResponse> send(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "알림 발송 요청 바디", required = true
            )
            @RequestBody SendNotificationRequest req
    ) throws Exception {
        return ApiData.ok(fcmCommandService.send(req));
    }

    /** 토큰 조회 (로그인 유저 본인) */
    @GetMapping("/tokens")
    @Operation(
            summary = "내 FCM 토큰 목록 조회",
            description = "현재 로그인한 사용자의 모든 FCM/WebPush 토큰을 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiData<FcmTokensResponse> getLoginedUserFcmTokens(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) throws Exception {
        return ApiData.ok(
                FcmTokensResponse.fromEntities(
                        fcmQueryService.getByUser(principalDetails.getUser())
                )
        );
    }


}
