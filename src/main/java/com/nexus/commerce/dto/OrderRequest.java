package com.nexus.commerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "Please inform a customer ID")
        @Positive(message = "Customer ID must be positive")
        Long customerId,

        @NotEmpty(message = "Please inform at least one item")
        List<@Valid OrderRequestItens> itens
) {
    public record OrderRequestItens(
            @NotNull(message = "Please inform a product ID")
            @Positive(message = "Product ID must be positive")
            Long productId,

            @NotNull(message = "Please inform the requested quantity")
            @Positive(message = "The requested quantity must be greater than zero")
            Integer quantity
    ) {
    }
}