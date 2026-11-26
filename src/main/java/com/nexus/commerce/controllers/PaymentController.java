package com.nexus.commerce.controllers;

import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.dto.ProcessPaymentRequest;
import com.nexus.commerce.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/process")
    public ResponseEntity<PaymentGatewayResponse> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(service.processPayment(request));
    }
}
