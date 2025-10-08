package com.ocean.piuda.store.repository;

import com.ocean.piuda.store.dto.StoreWithStatsDto;
import com.ocean.piuda.store.entity.Store;
import com.ocean.piuda.store.enums.StoreCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByCategoryOrderByCreatedAtDesc(StoreCategory category);

    List<Store> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM Store s WHERE s.owner.id = :ownerId ORDER BY s.createdAt DESC")
    List<Store> findAllByOwnerIdOrderByCreatedAtDesc(@Param("ownerId") Long ownerId);

    // 페이징 처리된 가게 목록 조회
    Page<Store> findAllBy(Pageable pageable);

    // 카테고리별 페이징 처리된 가게 목록 조회
    Page<Store> findAllByCategory(StoreCategory category, Pageable pageable);

    // 단건 + 통계 (리뷰/구독 없음 → 기본값으로 채움, 주문 수만 실제 계산)
    @Query("""
            SELECT new com.ocean.piuda.store.dto.StoreWithStatsDto(
              s,
              0.0,
              0L,
              COUNT(DISTINCT o.id),
              false
            )
            FROM Store s
            LEFT JOIN Order o  ON o.store.id  = s.id
            WHERE s.id = :storeId
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            """)
    StoreWithStatsDto findByIdWithStats(@Param("storeId") Long storeId,
                                        @Param("userId") Long userId);
    // ^ userId는 현재 구독 여부 계산에 사용되지 않으나, 기존 시그니처 호환을 위해 유지

    // "구독한 가게" 대체: 사용자가 주문한 이력이 있는 가게 목록 + 통계
    @Query("""
            SELECT new com.ocean.piuda.store.dto.StoreWithStatsDto(
                s,
                0.0,
                0L,
                COUNT(DISTINCT o.id),
                true
            )
            FROM Store s
            INNER JOIN Order o ON o.store.id = s.id
            WHERE o.user.id = :userId
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            ORDER BY s.createdAt DESC
            """)
    List<StoreWithStatsDto> findSubscribedStoresWithStats(@Param("userId") Long userId);

    // 카테고리별 목록(최신순) + 통계
    @Query("""
            SELECT new com.ocean.piuda.store.dto.StoreWithStatsDto(
              s,
              0.0,
              0L,
              COUNT(DISTINCT o.id),
              false
            )
            FROM Store s
            LEFT JOIN Order  o ON o.store.id = s.id
            WHERE s.category = :category
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            ORDER BY s.createdAt DESC
            """)
    Page<StoreWithStatsDto> findAllByCategoryWithStats(@Param("userId") Long userId,
                                                       @Param("category") StoreCategory category,
                                                       Pageable pageable);

    // 카테고리별 인기순(주문 수 DESC) + 통계
    @Query("""
            SELECT new com.ocean.piuda.store.dto.StoreWithStatsDto(
              s,
              0.0,
              0L,
              COUNT(DISTINCT o.id),
              false
            )
            FROM Store s
            LEFT JOIN Order  o ON o.store.id = s.id
            WHERE s.category = :category
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            ORDER BY COUNT(DISTINCT o.id) DESC, s.createdAt DESC
            """)
    Page<StoreWithStatsDto> findAllByCategoryAndPopularityWithStats(@Param("userId") Long userId,
                                                                    @Param("category") StoreCategory category,
                                                                    Pageable pageable);

    // 카테고리별 별점순: 리뷰 엔티티 없으므로 최신순과 동일(필요 시 제거/주석 권장)
    @Query("""
            SELECT new com.ocean.piuda.store.dto.StoreWithStatsDto(
              s,
              0.0,
              0L,
              COUNT(DISTINCT o.id),
              false
            )
            FROM Store s
            LEFT JOIN Order  o ON o.store.id = s.id
            WHERE s.category = :category
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            ORDER BY s.createdAt DESC
            """)
    Page<StoreWithStatsDto> findAllByCategoryAndRatingWithStats(@Param("userId") Long userId,
                                                                @Param("category") StoreCategory category,
                                                                Pageable pageable);

    // 카테고리별 리뷰수순: 리뷰 엔티티 없으므로 인기순과 동일(주문 수 기준)
    @Query("""
            SELECT new com.ocean.piuda.store.dto.StoreWithStatsDto(
              s,
              0.0,
              0L,
              COUNT(DISTINCT o.id),
              false
            )
            FROM Store s
            LEFT JOIN Order  o ON o.store.id = s.id
            WHERE s.category = :category
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            ORDER BY COUNT(DISTINCT o.id) DESC, s.createdAt DESC
            """)
    Page<StoreWithStatsDto> findAllByCategoryAndReviewCountWithStats(@Param("userId") Long userId,
                                                                     @Param("category") StoreCategory category,
                                                                     Pageable pageable);

    // 통합 검색: 메뉴/가게명 접두사 매칭 + 통계 (리뷰 제거)
    @Query("""
            SELECT DISTINCT new com.ocean.piuda.store.dto.StoreWithStatsDto(
                s,
                0.0,
                0L,
                COUNT(DISTINCT o.id),
                false
            )
            FROM Store s
            LEFT JOIN Menu m ON m.store.id = s.id AND m.isActive = true
            LEFT JOIN Order o ON o.store.id = s.id
            WHERE (s.name LIKE CONCAT(:keyword, '%') OR m.name LIKE CONCAT(:keyword, '%'))
            GROUP BY s.id, s.name, s.category, s.description, s.address, s.phoneNumber,
                     s.openingHours, s.bannerImageUrl, s.isOpen, s.bizRegNo, s.owner.id,
                     s.createdAt, s.modifiedAt
            ORDER BY s.createdAt DESC
            """)
    Page<StoreWithStatsDto> searchStoresByKeyword(@Param("keyword") String keyword,
                                                  @Param("userId") Long userId,
                                                  Pageable pageable);
}
