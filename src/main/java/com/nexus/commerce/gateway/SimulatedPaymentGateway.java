package com.nexus.commerce.gateway;

import com.nexus.commerce.domain.enums.GatewayMode;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentMethod;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    // Regra fictícia, pagamentos acima de 10000 não podem ser realizados por boleto
    private static final BigDecimal MAX_TICKET_VALUE = BigDecimal.valueOf(10000L);

    private final ConcurrentMap<UUID, PaymentGatewayResponse> processedTransactions = new ConcurrentHashMap<>();

    @Value("${GATEWAY_MODE:SUCCESS}")
    private GatewayMode gatewayMode;

    @Override
    public PaymentGatewayResponse simulate(PaymentGatewayRequest request) {
        Optional<PaymentGatewayResponse> previousResponse = findByIdempotencyKey(request.idempotencyKey());
        if (previousResponse.isPresent()) {
            return previousResponse.get();
        }

        if (gatewayMode == GatewayMode.UNAVAILABLE) {
            throw new GatewayUnavailableException("The gateway is unavailable");
        }

        PaymentGatewayResponse response = buildResponse(request);
        PaymentGatewayResponse existingResponse = processedTransactions.putIfAbsent(request.idempotencyKey(), response);
        PaymentGatewayResponse finalResponse = existingResponse == null ? response : existingResponse;

        if (gatewayMode == GatewayMode.TIMEOUT && existingResponse == null) {
            throw new GatewayTimeoutException("The gateway processed the payment but the response timed out");
        }

        return finalResponse;
    }

    private PaymentGatewayResponse buildResponse(PaymentGatewayRequest request) {
        if (gatewayMode == GatewayMode.FAILED
                || (request.paymentMethod().equals(PaymentMethod.TICKET)
                && request.amount().compareTo(MAX_TICKET_VALUE) >= 0)) {
            return new PaymentGatewayResponse(
                    UUID.randomUUID().toString(),
                    PaymentAttemptStatus.FAILED,
                    "The payment was rejected by the simulated gateway");
        }

        return new PaymentGatewayResponse(
                UUID.randomUUID().toString(),
                PaymentAttemptStatus.SUCCESS,
                null);
    }

    @Override
    public Optional<PaymentGatewayResponse> findByIdempotencyKey(UUID idempotencyKey) {
        return Optional.ofNullable(processedTransactions.get(idempotencyKey));
    }
}
