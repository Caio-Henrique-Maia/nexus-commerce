package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentMethod;
import com.nexus.commerce.domain.enums.PaymentStatus;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.exceptions.ConflictException;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.repositories.PaymentAttemptRepository;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentAttemptService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public PaymentAttempt prepareAttempt(UUID paymentId, PaymentMethod requestedMethod) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        Order order = payment.getOrder();
        if (payment.getPaymentStatus() == PaymentStatus.EXPIRED
                || order.getOrderStatus() == OrderStatus.CANCELED
                || order.getOrderStatus() == OrderStatus.EXPIRED) {
            throw new ConflictException("This order is no longer available for payment");
        }

        if (payment.getExpiresAt() != null && !payment.getExpiresAt().isAfter(Instant.now())) {
            throw new ConflictException("This payment has expired");
        }

        PaymentAttempt previousAttempt = paymentAttemptRepository
                .findTopByPayment_IdOrderByAttemptedAtDescIdDesc(paymentId)
                .orElse(null);

        if (payment.getPaymentStatus() == PaymentStatus.CONFIRMED) {
            if (previousAttempt == null
                    || previousAttempt.getPaymentAttemptStatus() != PaymentAttemptStatus.SUCCESS) {
                throw new IllegalStateException("Confirmed payment has no successful payment attempt");
            }
            return previousAttempt;
        }

        if (previousAttempt != null
                && (previousAttempt.getPaymentAttemptStatus() == PaymentAttemptStatus.UNKNOWN
                || previousAttempt.getPaymentAttemptStatus() == PaymentAttemptStatus.PROCESSING
                || previousAttempt.getPaymentAttemptStatus() == PaymentAttemptStatus.PENDING)) {
            return previousAttempt;
        }

        PaymentMethod method = requestedMethod != null ? requestedMethod : payment.getPaymentMethod();
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setPaymentMethod(method);
        attempt.setIdempotencyKey(UUID.randomUUID());
        attempt.setPaymentAttemptStatus(PaymentAttemptStatus.PROCESSING);

        return paymentAttemptRepository.save(attempt);
    }

    @Transactional
    public void updateAttempt(Long attemptId, PaymentGatewayResponse response) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found"));

        Payment payment = paymentRepository.findByIdForUpdate(attempt.getPayment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS
                || attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.FAILED) {
            return;
        }

        if (payment.getPaymentStatus() == PaymentStatus.EXPIRED
                || payment.getOrder().getOrderStatus() == OrderStatus.EXPIRED
                || payment.getOrder().getOrderStatus() == OrderStatus.CANCELED) {
            throw new ConflictException("The order is no longer available for payment");
        }

        attempt.setPaymentAttemptStatus(response.paymentAttemptStatus());
        attempt.setGatewayTransactionId(response.transactionId());
        attempt.setFailureReason(response.failureReason());

        if (response.paymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {
            payment.setPaymentStatus(PaymentStatus.CONFIRMED);
            payment.getOrder().setOrderStatus(OrderStatus.PROCESSING);
        } else if (response.paymentAttemptStatus() == PaymentAttemptStatus.FAILED) {
            payment.setPaymentStatus(PaymentStatus.DENIED);
        }

        paymentAttemptRepository.save(attempt);
    }
}
