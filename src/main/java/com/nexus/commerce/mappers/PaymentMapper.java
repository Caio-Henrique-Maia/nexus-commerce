package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.dto.PaymentRequest;
import com.nexus.commerce.dto.PaymentResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "order.id", source = "orderId")
    Payment toEntity(PaymentRequest paymentRequest);

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(Payment payment);
}