package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentGatewayRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod
) {
}
