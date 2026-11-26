package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentStatus;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.dto.ProcessPaymentRequest;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.gateway.PaymentGateway;
import com.nexus.commerce.repositories.PaymentAttemptRepository;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentGateway gateway;

    private Payment findByIdOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private PaymentGatewayRequest buildGatewayRequest(Payment payment) {
        return new PaymentGatewayRequest(payment.getPaidAmount(), payment.getPaymentMethod());
    }

    private void updatePaymentStatus(Payment payment, PaymentAttemptStatus attemptStatus) {
        if (attemptStatus == PaymentAttemptStatus.SUCCESS) {
            payment.setPaymentStatus(PaymentStatus.CONFIRMED);
        } else if (attemptStatus == PaymentAttemptStatus.FAILED) {
            payment.setPaymentStatus(PaymentStatus.DENIED);
        }
    }

    @Transactional
    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        Payment payment = findByIdOrThrow(request.paymentId());

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setPaymentAttemptStatus(PaymentAttemptStatus.PROCESSING);
        paymentAttemptRepository.save(attempt);

        PaymentGatewayResponse response = gateway.simulate(buildGatewayRequest(payment));

        attempt.setPaymentAttemptStatus(response.paymentAttemptStatus());
        attempt.setGatewayTransactionId(response.transactionId());
        attempt.setFailureReason(response.failureReason());

        updatePaymentStatus(payment, response.paymentAttemptStatus());

        return response;
    }
}
