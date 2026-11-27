package com.nexus.commerce.domain.entity;

import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempt")
@Getter
@Setter
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @CreationTimestamp
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private Instant attemptedAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_attempt_status", nullable = false, length = 30)
    private PaymentAttemptStatus paymentAttemptStatus;

    @Column(name = "gateway_transaction_id", length = 300)
    private String gatewayTransactionId;

    @Column(name = "failure_reason", length = 400)
    private String failureReason;
}
