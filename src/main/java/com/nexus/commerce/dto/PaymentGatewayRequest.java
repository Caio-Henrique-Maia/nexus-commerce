package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGatewayRequest(
        @NotBlank(message = "Please provide a payment id")
        UUID paymentId,

        @Positive(message = "The amount to be paid must be greater then zero")
        BigDecimal amount,

        @NotBlank(message = "Please inform a Payment Method")
        PaymentMethod paymentMethod
) {
}
