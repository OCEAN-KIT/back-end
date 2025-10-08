package com.ocean.piuda.notification.entity;


import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.notification.enums.Platform;
import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 브라우저/기기 식별자 (optional)
     */
    @Column(length = 100)
    private String deviceId;

    /**
     * 플랫폼/OS (optional)
     */
    @Enumerated(EnumType.STRING)
    private Platform platform;

    /** FCM 등록 토큰 */
    @Column(nullable = false, unique = true, length = 512)
    private String token;


    public void upsertUser(User user) {
        this.user = user;
    }

    public void updateClientInfo(String deviceId, Platform platform) {
        this.deviceId = deviceId;
        this.platform = platform;
    }

}
