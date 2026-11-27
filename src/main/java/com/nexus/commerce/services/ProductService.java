package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Product;
import com.nexus.commerce.dto.ProductRequest;
import com.nexus.commerce.dto.ProductResponse;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.mappers.ProductMapper;
import com.nexus.commerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public Product getProduct(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Product not found"));
    }

    public List<ProductResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public ProductResponse findById(Long id) {
        return mapper.toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        mapper.update(request, product);
        return mapper.toResponse(repository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        try {
            repository.delete(product);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DataIntegrityViolationException("Product cannot be deleted because it is referenced by an order item", exception);
        }
    }}