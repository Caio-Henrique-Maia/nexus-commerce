package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.Payment;
import com.nexus.commerce.domain.entity.PaymentAttempt;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.dto.PaymentRequest;
import com.nexus.commerce.dto.PaymentResponse;
import com.nexus.commerce.dto.ProcessPaymentRequest;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.gateway.GatewayException;
import com.nexus.commerce.gateway.PaymentGateway;
import com.nexus.commerce.mappers.PaymentMapper;
import com.nexus.commerce.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentGateway gateway;
    private final OrderService orderService;
    private final PaymentMapper mapper;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Order order = orderService.findByIdOrThrow(request.orderId());

        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A payment already exists for this order");
        }

        BigDecimal amount = order.getTotalAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The order total must be greater than zero");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaidAmount(amount);
        order.setOrderStatus(OrderStatus.PAYMENT_PENDING);

        return mapper.toResponse(paymentRepository.save(payment));
    }

    private Payment findByOrderOrThrow(Long orderId) {
        return paymentRepository.findByOrder(orderService.findByIdOrThrow(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private PaymentGatewayRequest buildGatewayRequest(Payment payment, UUID idempotencyKey) {
        return new PaymentGatewayRequest(payment.getPaidAmount(), payment.getPaymentMethod(), idempotencyKey);
    }

    private PaymentGatewayResponse buildResponse(PaymentAttempt attempt) {
        return new PaymentGatewayResponse(
                attempt.getGatewayTransactionId(),
                attempt.getPaymentAttemptStatus(),
                attempt.getFailureReason());
    }

    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        Payment payment = findByOrderOrThrow(request.orderId());

        PaymentAttempt attempt = paymentAttemptService.prepareAttempt(payment.getId());

        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {
            return buildResponse(attempt);
        }

        PaymentGatewayResponse response;
        try {
            response = gateway.findByIdempotencyKey(attempt.getIdempotencyKey())
                    .orElseGet(() -> gateway.simulate(buildGatewayRequest(payment, attempt.getIdempotencyKey())));
        } catch (GatewayException exception) {
            response = new PaymentGatewayResponse(
                    null,
                    PaymentAttemptStatus.UNKNOWN,
                    exception.getMessage());
        }

        paymentAttemptService.updateAttempt(attempt.getId(), response);
        return response;
    }
}