package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.OrderItem;
import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentStatus;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.repositories.PaymentAttemptRepository;
import com.nexus.commerce.repositories.PaymentRepository;
import com.nexus.commerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentExpirationService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ProductRepository productRepository;

    @Transactional
    public boolean expireIfDue(UUID paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getExpiresAt() == null || payment.getExpiresAt().isAfter(Instant.now())) {
            return false;
        }

        if (payment.getPaymentStatus() == PaymentStatus.CONFIRMED
                || payment.getPaymentStatus() == PaymentStatus.EXPIRED) {
            return false;
        }

        Order order = payment.getOrder();
        if (order.getOrderStatus() == OrderStatus.CANCELED
                || order.getOrderStatus() == OrderStatus.EXPIRED
                || order.getOrderStatus() == OrderStatus.PROCESSING) {
            return false;
        }

        PaymentAttempt latestAttempt = paymentAttemptRepository
                .findTopByPayment_IdOrderByAttemptedAtDescIdDesc(paymentId)
                .orElse(null);

        if (latestAttempt != null && isPaymentInProgress(latestAttempt)) {
            return false;
        }

        for (OrderItem item : order.getItems()) {
            int updatedRows = productRepository.releaseStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );

            if (updatedRows != 1) {
                throw new IllegalStateException(
                        "Could not release stock for product " + item.getProduct().getId()
                );
            }
        }

        payment.setPaymentStatus(PaymentStatus.EXPIRED);
        order.setOrderStatus(OrderStatus.EXPIRED);
        return true;
    }

    private boolean isPaymentInProgress(PaymentAttempt attempt) {
        return attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.PENDING
                || attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.PROCESSING
                || attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.UNKNOWN;
    }
}
