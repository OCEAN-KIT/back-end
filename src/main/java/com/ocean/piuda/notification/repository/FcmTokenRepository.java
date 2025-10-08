package com.ocean.piuda.notification.repository;


import com.ocean.piuda.notification.entity.FcmToken;
import com.ocean.piuda.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);
    List<FcmToken> findAllByUser(User user);
    void deleteByToken(String token);
    void deleteAllByUser(User user);
}
