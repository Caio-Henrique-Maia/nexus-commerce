package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGatewayRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        UUID idempotencyKey
) {
}
