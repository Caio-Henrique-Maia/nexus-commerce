package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentStatus;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.repositories.PaymentAttemptRepository;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentAttemptService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public PaymentAttempt prepareAttempt(UUID paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        PaymentAttempt previousAttempt = paymentAttemptRepository
                .findTopByPayment_IdOrderByAttemptedAtDesc(paymentId)
                .orElse(null);

        if (payment.getPaymentStatus() == PaymentStatus.CONFIRMED) {
            if (previousAttempt == null) {
                throw new IllegalStateException("Confirmed payment has no payment attempt");
            }
            return previousAttempt;
        }

        if (previousAttempt != null
                && (previousAttempt.getPaymentAttemptStatus() == PaymentAttemptStatus.UNKNOWN
                || previousAttempt.getPaymentAttemptStatus() == PaymentAttemptStatus.PROCESSING)) {
            return previousAttempt;
        }

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setIdempotencyKey(UUID.randomUUID());
        attempt.setPaymentAttemptStatus(PaymentAttemptStatus.PROCESSING);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return paymentAttemptRepository.save(attempt);
    }

    @Transactional
    public void updateAttempt(Long attemptId, PaymentGatewayResponse response) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found"));

        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS
                || attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.FAILED) {
            return;
        }

        attempt.setPaymentAttemptStatus(response.paymentAttemptStatus());
        attempt.setGatewayTransactionId(response.transactionId());
        attempt.setFailureReason(response.failureReason());

        Payment payment = attempt.getPayment();
        if (response.paymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {
            payment.setPaymentStatus(PaymentStatus.CONFIRMED);
        } else if (response.paymentAttemptStatus() == PaymentAttemptStatus.FAILED) {
            payment.setPaymentStatus(PaymentStatus.DENIED);
        }

        paymentAttemptRepository.save(attempt);
    }
}
