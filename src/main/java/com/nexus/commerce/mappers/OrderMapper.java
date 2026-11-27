package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.OrderItem;
import com.nexus.commerce.dto.OrderItemResponse;
import com.nexus.commerce.dto.OrderResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(target = "customerId", source = "customer.id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(
            target = "subtotal",
            expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))"
    )
    OrderItemResponse toItemResponse(OrderItem item);
}