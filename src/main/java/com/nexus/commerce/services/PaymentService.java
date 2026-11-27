package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.*;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.domain.enums.PaymentAttemptStatus;
import com.nexus.commerce.domain.enums.PaymentStatus;
import com.nexus.commerce.dto.PaymentGatewayRequest;
import com.nexus.commerce.dto.PaymentGatewayResponse;
import com.nexus.commerce.dto.PaymentRequest;
import com.nexus.commerce.dto.PaymentResponse;
import com.nexus.commerce.dto.ProcessPaymentRequest;
import com.nexus.commerce.exceptions.ConflictException;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.gateway.GatewayException;
import com.nexus.commerce.gateway.PaymentGateway;
import com.nexus.commerce.mappers.PaymentMapper;
import com.nexus.commerce.repositories.OrderRepository;
import com.nexus.commerce.repositories.PaymentRepository;
import com.nexus.commerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentGateway gateway;
    private final OrderService orderService;
    private final PaymentMapper mapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Order order = orderService.findByIdOrThrow(request.orderId());

        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new ConflictException("A payment already exists for this order");
        }

        BigDecimal amount = order.getTotalAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("The order total must be greater than zero");
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

    private Order findByPaymentOrThrow(Payment payment) {
        return orderRepository.findByPayment(payment)
                .orElseThrow(() -> new ResourceNotFoundException("No order found for this payment"));
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

    private void updateStock(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.DENIED);
        paymentRepository.save(payment);

        Order order = findByPaymentOrThrow(payment);
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        Set<OrderItem> items = order.getItems();

        List<Product> products = items.stream()
                .map(i -> productRepository.findById(i.getId())
                        .map(p -> {
                            p.setStockQuantity(p.getStockQuantity() + i.getQuantity());
                            return p;
                        }))
                .flatMap(Optional::stream)
                .toList();

        productRepository.saveAll(products);
    }

    public PaymentGatewayResponse processPayment(ProcessPaymentRequest request) {
        Payment payment = findByOrderOrThrow(request.orderId());

        if (findByPaymentOrThrow(payment).getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("This order is canceled and can no longer be paid");
        }

        if (payment.getExpiresAt().isBefore(Instant.now())) {
            updateStock(payment);
            throw new IllegalStateException("This order can no longer be paid due to this max expiration");
        }

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