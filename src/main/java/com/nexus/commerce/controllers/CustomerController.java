package com.nexus.commerce.controllers;

import com.nexus.commerce.dto.CustomerRequest;
import com.nexus.commerce.services.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/customer")
public class CustomerController {

    private CustomerService service;

    @PostMapping()
    public ResponseEntity<Void> create(@Valid @RequestBody CustomerRequest request) {
        service.create(request);
        return ResponseEntity.accepted().build();
    }
}
