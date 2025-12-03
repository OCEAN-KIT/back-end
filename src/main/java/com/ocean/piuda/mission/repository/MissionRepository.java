package com.ocean.piuda.mission.repository;

import com.ocean.piuda.bio.enums.BioGroup;
import com.ocean.piuda.mission.domain.Mission;
import com.ocean.piuda.mission.enums.MissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MissionRepository extends JpaRepository<Mission, Long>, JpaSpecificationExecutor<Mission> {

    Page<Mission> findByStatus(MissionStatus status, Pageable pageable);

    Page<Mission> findByTargetBioGroup(BioGroup targetBioGroup, Pageable pageable);
}

