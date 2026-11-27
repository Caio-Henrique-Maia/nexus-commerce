package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.dto.ProcessPaymentRequest;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.gateway.GatewayException;
import com.nexus.commerce.gateway.PaymentGateway;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentGateway gateway;

    private Payment findByIdOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private PaymentGatewayRequest buildGatewayRequest(Payment payment, UUID idempotencyKey) {
        return new PaymentGatewayRequest(payment.getPaidAmount(), payment.getPaymentMethod(), idempotencyKey);
    }

    private PaymentGatewayResponse buildResponse(PaymentAttempt attempt) {
        return new PaymentGatewayResponse(
                attempt.getGatewayTransactionId(),
                attempt.getPaymentAttemptStatus(),
                attempt.getFailureReason());
    }

    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        Payment payment = findByIdOrThrow(request.paymentId());
        PaymentAttempt attempt = paymentAttemptService.prepareAttempt(payment.getId());

        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {
            return buildResponse(attempt);
        }

        PaymentGatewayResponse response;
        try {
            response = gateway.findByIdempotencyKey(attempt.getIdempotencyKey())
                    .orElseGet(() -> gateway.simulate(buildGatewayRequest(payment, attempt.getIdempotencyKey())));
        } catch (GatewayException exception) {
            response = new PaymentGatewayResponse(
                    null,
                    PaymentAttemptStatus.UNKNOWN,
                    exception.getMessage());
        }

        paymentAttemptService.updateAttempt(attempt.getId(), response);
        return response;
    }
}
