package com.nexus.commerce.gateway;

import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentMethod;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    // Regra fictícia, pagamentos acima de 10000 não podem ser realizados por boleto
    private static final BigDecimal MAX_TICKET_VALUE = BigDecimal.valueOf(10000L);

    @Override
    public PaymentGatewayResponse simulate(PaymentGatewayRequest request) {
        if (request.paymentMethod().equals(PaymentMethod.TICKET) && request.amount().compareTo(MAX_TICKET_VALUE) >= 1) {
            return new PaymentGatewayResponse(
                    UUID.randomUUID().toString(),
                    PaymentAttemptStatus.FAILED,
                    "For payments beyond 10.000 this Payment Method is not allowed");

        }

        return new PaymentGatewayResponse(
                UUID.randomUUID().toString(),
                PaymentAttemptStatus.SUCCESS,
                null);
    }
}
