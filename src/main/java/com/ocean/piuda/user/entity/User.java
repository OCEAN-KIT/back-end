package com.ocean.piuda.user.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import com.ocean.piuda.security.jwt.dto.request.UserUpdateRequestDto;
import com.ocean.piuda.security.jwt.enums.Role;
import com.ocean.piuda.security.oauth2.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    private String providerId;

    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 부가 정보
    private String nickname;
    private String email;
    private String phone;

    /**
     * 한 유저당 최대 1개의 워치만 등록 (0개 허용)
     * 암호화된 deviceId 기준으로 관리
     */
    @Column(name = "watch_device_id", length = 100, unique = true)
    private String watchDeviceId;

    public void updateRole(Role role){
        this.role = role;
    }

    public void update(UserUpdateRequestDto dto) {
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            this.nickname = dto.getNickname();
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            this.email = dto.getEmail();
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            this.phone = dto.getPhone();
        }
    }

    public void pairWatch(String watchDeviceId) {
        this.watchDeviceId = watchDeviceId;
    }

    public void unpairWatch() {
        this.watchDeviceId = null;
    }
}
