package com.nexus.commerce.repositories;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT payment FROM Payment payment WHERE payment.id = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") UUID paymentId);

    Optional<Payment> findByOrder(Order order);

    @Query("""
            SELECT payment.id
            FROM Payment payment
            WHERE payment.expiresAt <= :now
              AND payment.paymentStatus <> :confirmed
              AND payment.paymentStatus <> :expired
            """)
    List<UUID> findExpiredPaymentIds(
            @Param("now") Instant now,
            @Param("confirmed") PaymentStatus confirmed,
            @Param("expired") PaymentStatus expired
    );
}
