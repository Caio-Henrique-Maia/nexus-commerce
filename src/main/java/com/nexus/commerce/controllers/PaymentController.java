package com.nexus.commerce.controllers;

import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.dto.PaymentRequest;
import com.nexus.commerce.dto.PaymentResponse;
import com.nexus.commerce.dto.ProcessPaymentRequest;
import com.nexus.commerce.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(request));
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentGatewayResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(service.processPayment(request));
    }
}