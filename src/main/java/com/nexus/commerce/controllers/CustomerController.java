package com.nexus.commerce.controllers;

import com.nexus.commerce.dto.CustomerRequest;
import com.nexus.commerce.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CustomerRequest request) {
        service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}