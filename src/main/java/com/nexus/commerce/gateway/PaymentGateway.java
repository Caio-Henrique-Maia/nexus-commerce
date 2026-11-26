package com.nexus.commerce.gateway;

import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;

public interface PaymentGateway {
    PaymentGatewayResponse simulate(PaymentGatewayRequest request);
}
