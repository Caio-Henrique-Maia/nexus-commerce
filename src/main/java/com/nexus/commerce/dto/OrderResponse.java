package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.OrderStatus;
import java.math.BigDecimal;

public record OrderResponse(Long id, Long customerId, BigDecimal totalAmount, OrderStatus orderStatus) { }