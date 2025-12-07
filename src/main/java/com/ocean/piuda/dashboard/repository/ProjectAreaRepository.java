package com.ocean.piuda.dashboard.repository;

import com.ocean.piuda.dashboard.entity.ProjectArea;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectAreaRepository extends JpaRepository<ProjectArea, Long> {
    // 필요한 데이터는 Service의 DTO 변환 과정에서 Batch Size 설정에 의해 효율적으로 로딩됨 (N+1 문제 방지)
}