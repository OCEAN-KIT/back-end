package com.ocean.piuda.payment.repository;

import com.ocean.piuda.payment.entity.Order;
import com.ocean.piuda.payment.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o.id from Order o where o.user.id = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    // 문자열 'AFTER_PAY' 대신 Enum 리터럴로 비교
    @Query("""
           select coalesce(sum(o.totalDiscountedPrice), 0)
           from Order o
           where o.user.id = :userId
             and o.status = com.ocean.piuda.payment.enums.OrderStatus.AFTER_PAY
           """)
    Long sumDiscountedByUserId(@Param("userId") Long userId);

    @Query("""
           select coalesce(sum(o.totalDiscountedPrice), 0)
           from Order o
           where o.user.id = :userId
             and o.status = com.ocean.piuda.payment.enums.OrderStatus.AFTER_PAY
             and o.createdAt between :start and :end
           """)
    Long sumDiscountedPriceByUserInPeriod(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("""
           select coalesce(sum(o.totalPrice), 0)
           from Order o
           where o.user.id = :userId
             and o.status = com.ocean.piuda.payment.enums.OrderStatus.AFTER_PAY
             and o.createdAt between :start and :end
           """)
    Long sumPaymentPriceByUserInPeriod(@Param("userId") Long userId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    List<Order> findByStoreIdOrderByIdDesc(Long storeId);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId,
                                         org.springframework.data.domain.Pageable pageable);

    @Query("""
           SELECT COUNT(o)
           FROM Order o
           WHERE o.store.id = :storeId
             AND o.user.id = :userId
             AND o.status = com.ocean.piuda.payment.enums.OrderStatus.AFTER_PAY
           """)
    long countCompletedOrders(@Param("storeId") Long storeId, @Param("userId") Long userId);
}
