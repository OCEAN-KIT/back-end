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
    // 이메일을 기준으로 사용자 찾기
    Optional<User> findByUsername(String username);

    //OAuth 정보( provider 벤더명과 해당 provider 상의 식별자값 ) 기준으로 사용자 찾기
    @Query("select u from User u where u.providerType = :providerType and u.providerId = :providerId " )
    Optional<User> findByOAuthInfo(@Param("providerType") ProviderType providerType,
                                   @Param("providerId") String providerId);



    // 검색: nickname 전용
    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    Page<User> searchByNicknameFulltext(@Param("q") String q, Pageable pageable);

    // 검색: username 전용
    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    Page<User> searchByUsernameFulltext(@Param("q") String q, Pageable pageable);

    // 검색: nickname + username 동시
    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
           """)
    Page<User> searchByNicknameOrUsernameFulltext(@Param("q") String q, Pageable pageable);




}
