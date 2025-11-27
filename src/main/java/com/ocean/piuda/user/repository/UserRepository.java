package com.ocean.piuda.user.repository;

import com.ocean.piuda.security.oauth2.enums.ProviderType;
import com.ocean.piuda.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    Optional<User> findByUsername(String username);

    @Query("select u from User u where u.providerType = :providerType and u.providerId = :providerId " )
    Optional<User> findByOAuthInfo(@Param("providerType") ProviderType providerType,
                                   @Param("providerId") String providerId);

    // 워치 암호화 ID 기준 조회
    Optional<User> findByWatchDeviceId(String watchDeviceId);

    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    Page<User> searchByNicknameFulltext(@Param("q") String q, Pageable pageable);

    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    Page<User> searchByUsernameFulltext(@Param("q") String q, Pageable pageable);

    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    Page<User> searchByNicknameOrUsernameFulltext(@Param("q") String q, Pageable pageable);
}
