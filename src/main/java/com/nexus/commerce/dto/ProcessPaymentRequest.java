package com.nexus.commerce.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProcessPaymentRequest(
        @NotNull(message = "Please provide a order id")
        Long orderId
) {
}
