package com.ocean.piuda.store.repository;

import com.ocean.piuda.store.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.menuCategory WHERE m.store.id = :storeId")
    List<Menu> findAllByStoreIdWithCategory(@Param("storeId") Long storeId);

    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId ORDER BY m.createdAt")
    List<Menu> findAllByStoreId(@Param("storeId") Long storeId);

    Optional<Menu> findByIdAndIsActiveTrue(Long id);
    List<Menu> findByStoreIdAndIsActiveTrue(Long storeId);

    @Query("SELECT m FROM Menu m WHERE m.isActive = true AND m.discount IS NOT NULL ORDER BY m.discount.percentage DESC")
    List<Menu> findTopDiscountMenus(org.springframework.data.domain.Pageable pageable);
}
