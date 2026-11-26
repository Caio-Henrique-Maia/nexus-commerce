package com.nexus.commerce.domain.entity;

import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "payment_attempt")
@Getter
@Setter
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @CreationTimestamp
    @Column(name = "attempted_at", updatable = false)
    private Instant attempted_at;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_attempt_status")
    private PaymentAttemptStatus paymentAttemptStatus;

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @Column(name = "failureReason")
    private String failureReason;

    @PrePersist
    private void setPaymentAttemptStatus() {
        this.paymentAttemptStatus = PaymentAttemptStatus.PENDING;
    }

}
