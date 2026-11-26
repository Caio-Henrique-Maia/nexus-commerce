package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotNull(message = "Please inform an order ID")
        @Positive(message = "Order ID must be positive")
        Long orderId,

        @NotNull(message = "Please inform a valid payment method")
        PaymentMethod paymentMethod
) {
}
