package com.ocean.piuda.payment.repository;

import com.ocean.piuda.payment.entity.Payment;
import com.ocean.piuda.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    Optional<PaymentHistory> findPaymentHistoriesByPayment(Payment payment);
}
