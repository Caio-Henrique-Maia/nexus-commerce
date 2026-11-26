package com.nexus.commerce.dto;

import com.nexus.commerce.domain.enums.PaymentAttemptStatus;

public record PaymentGatewayResponse(
        String transactionId,
        PaymentAttemptStatus paymentAttemptStatus,
        String failureReason
) {
}
