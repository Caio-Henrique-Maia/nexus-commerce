package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Please inform an order ID")
        @Positive(message = "Order ID must be positive")
        Long orderId,

        @NotBlank(message = "Please inform a valid payment method")
        PaymentMethod paymentMethod,

        @Positive(message = "Amount to be paid must be positive")
        BigDecimal paidAmount
) {
}
