package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.dto.CustomerRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {

    Customer toEntity(CustomerRequest customerRequest);

//    CustomerRequest toDto(Customer customer); DEIXANDO DESATIVADO POR ENQUANTO, SÓ ENQUANTO NÃO FAÇO O RESPONSE

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Customer partialUpdate(CustomerRequest customerRequest, @MappingTarget Customer customer);
}