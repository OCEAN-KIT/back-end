package com.ocean.piuda.admin.site.repository;

import com.ocean.piuda.admin.site.entity.SiteNameOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteNameOptionRepository extends JpaRepository<SiteNameOption, Long> {
    // 활성화된 목록만 조회 (사용자 화면용)
    List<SiteNameOption> findAllByIsActiveTrueOrderByCreatedAtDesc();

    // 전체 목록 (관리자용)
    List<SiteNameOption> findAllByOrderByCreatedAtDesc();

    // 중복 체크용
    boolean existsByName(String name);
}