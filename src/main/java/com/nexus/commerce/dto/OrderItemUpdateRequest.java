package com.nexus.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemUpdateRequest(
        @NotNull(message = "Please inform the item quantity")
        @Positive(message = "Item quantity must be greater than zero")
        Integer quantity
) { }