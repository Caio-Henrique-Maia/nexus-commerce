package com.nexus.commerce.mappers;

import com.nexus.commerce.domain.entity.Product;
import com.nexus.commerce.dto.ProductRequest;
import com.nexus.commerce.dto.ProductResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    void update(ProductRequest request, @MappingTarget Product product);
}