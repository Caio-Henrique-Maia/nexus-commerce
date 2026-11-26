package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.dto.PaymentRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "order.id", source = "orderId")
    Payment toEntity(PaymentRequest paymentRequest);
}