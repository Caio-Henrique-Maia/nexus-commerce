package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;
import com.nexus.commerce.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Long orderId,
        PaymentMethod paymentMethod,
        BigDecimal paidAmount,
        PaymentStatus paymentStatus
) {
}