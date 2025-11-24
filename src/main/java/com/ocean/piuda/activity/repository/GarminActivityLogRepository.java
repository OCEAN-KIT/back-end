package com.ocean.piuda.activity.repository;


import com.ocean.piuda.activity.entity.GarminActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarminActivityLogRepository extends JpaRepository<GarminActivityLog, Long> {
}
