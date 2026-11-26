package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.dto.CustomerRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Customer toEntity(CustomerRequest customerRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Customer partialUpdate(CustomerRequest customerRequest, @MappingTarget Customer customer);
}