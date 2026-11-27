package com.nexus.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCustomerUpdateRequest(
        @NotNull(message = "Please inform a customer ID")
        @Positive(message = "Customer ID must be positive")
        Long customerId
) {
}