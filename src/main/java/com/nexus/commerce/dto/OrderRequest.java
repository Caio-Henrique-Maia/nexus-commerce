package com.nexus.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "Please inform a customer ID") // TODO: JÁ MUDAR PARA AUTENTICAÇÃO PARA TREINAR E INCLUSIVE TROCAR PARA MICRO-SERVIÇO
        @Positive(message = "Customer ID must be positive")
        Long customerId,
        @NotNull(message = "Please inform at least one item with quantity")
        List<OrderRequestItens> itens) {

    public record OrderRequestItens(
            Long productId,
            @Positive(message = "The requested quantity must be greater than zero")
            Integer quantity
    ) {
    }
}