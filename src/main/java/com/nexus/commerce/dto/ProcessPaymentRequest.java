package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentRequest(
        @NotNull(message = "Please provide an order id")
        Long orderId,
        PaymentMethod paymentMethod
) {
}
