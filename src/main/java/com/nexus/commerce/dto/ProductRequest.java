package com.nexus.commerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Please inform a product name")
        @Size(max = 150, message = "Product name must contain at most 150 characters")
        String name,
        @NotNull(message = "Please inform a product price")
        @DecimalMin(value = "0.01", message = "Product price must be greater than zero")
        BigDecimal price,
        @NotNull(message = "Please inform the stock quantity")
        @PositiveOrZero(message = "Stock quantity cannot be negative")
        Integer stockQuantity
) { }