package com.nexus.commerce.services;

import com.nexus.commerce.domain.enums.PaymentStatus;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentExpirationScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentExpirationService paymentExpirationService;

    @Scheduled(fixedDelayString = "${payments.expiration-scan-interval-ms:60000}")
    public void expirePayments() {
        for (UUID paymentId : paymentRepository.findExpiredPaymentIds(
                Instant.now(),
                PaymentStatus.CONFIRMED,
                PaymentStatus.EXPIRED
        )) {
            try {
                paymentExpirationService.expireIfDue(paymentId);
            } catch (RuntimeException exception) {
                log.error("Could not expire payment {}", paymentId, exception);
            }
        }
    }
}
