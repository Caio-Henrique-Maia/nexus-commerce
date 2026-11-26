package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.gateway.SimulatedPaymentGateway;
import com.nexus.commerce.repositories.PaymentAttemptRepository;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final SimulatedPaymentGateway gateway;

    private Payment findByIdOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }


    @Transactional
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        Payment payment = findByIdOrThrow(request.paymentId());

        PaymentGatewayResponse response = gateway.simulate(request);

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setPaymentAttemptStatus(response.paymentAttemptStatus());
        attempt.setGatewayTransactionId(response.transactionId());
        attempt.setFailureReason(response.failureReason());

        paymentAttemptRepository.save(attempt);

        return response;
    }
}
