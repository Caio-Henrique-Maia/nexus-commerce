package com.nexus.commerce.repositories;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    boolean existsByPayment(Payment payment);

    @Query(value = "SELECT MAX(attempted_at) FROM payment_attempt WHERE payment_id = :paymentId", nativeQuery = true)
    PaymentAttempt findMaxAttemptByPayment(@Param("paymentId") UUID paymentId);
}