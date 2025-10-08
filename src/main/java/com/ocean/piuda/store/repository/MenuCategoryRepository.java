package com.ocean.piuda.store.repository;

import com.ocean.piuda.store.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.store.id = :storeId ORDER BY mc.createdAt")
    List<MenuCategory> findAllByStoreId(@Param("storeId") Long storeId);
}
