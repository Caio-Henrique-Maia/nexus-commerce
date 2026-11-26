package com.nexus.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "Please inform an order ID")
        @Positive(message = "Order ID must be positive")
        Long orderId,
        @NotNull(message = "Please inform a product ID")
        @Positive(message = "Product ID must be positive")
        Long productId,
        @NotNull(message = "Please inform the item quantity")
        @Positive(message = "Item quantity must be greater than zero")
        Integer quantity
) { }