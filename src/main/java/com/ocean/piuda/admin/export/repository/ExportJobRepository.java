package com.ocean.piuda.admin.export.repository;

import com.ocean.piuda.admin.export.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
    List<ExportJob> findAllByOrderByCreatedAtDesc();
}
