package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.dto.CustomerRequest;
import com.nexus.commerce.mappers.CustomerMapper;
import com.nexus.commerce.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public void create(CustomerRequest req) {
        repository.save(mapper.toEntity(req));
    }
}
