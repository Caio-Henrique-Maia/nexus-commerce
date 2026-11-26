package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.dto.OrderResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(target = "customerId", source = "customer.id")
    OrderResponse toResponse(Order order);
}