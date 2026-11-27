package com.nexus.commerce.gateway;

import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;

import java.util.Optional;
import java.util.UUID;

public interface PaymentGateway {
    PaymentGatewayResponse simulate(PaymentGatewayRequest request);

    Optional<PaymentGatewayResponse> findByIdempotencyKey(UUID idempotencyKey);
}
