package com.ocean.piuda.payment.repository;


import com.ocean.piuda.payment.entity.OrderParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderParticipantRepository extends JpaRepository<OrderParticipant, Long> {
    List<OrderParticipant> findByOrderId(Long orderId);
    List<OrderParticipant> findByOrderIdIn(Collection<Long> orderIds);
}
