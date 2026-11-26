package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.dto.OrderRequest;
import com.nexus.commerce.dto.OrderResponse;
import com.nexus.commerce.mappers.OrderMapper;
import com.nexus.commerce.repositories.CustomerRepository;
import com.nexus.commerce.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository repository;
    private final CustomerRepository customerRepository;
    private final OrderMapper mapper;

    public List<OrderResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public OrderResponse findById(Long id) {
        return mapper.toResponse(getOrder(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = new Order();
        order.setCustomer(getCustomer(request.customerId()));
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderStatus(OrderStatus.CREATED);
        return mapper.toResponse(repository.save(order));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = getOrder(id);
        order.setCustomer(getCustomer(request.customerId()));
        return mapper.toResponse(repository.save(order));
    }

    @Transactional
    public void delete(Long id) {
        Order order = getOrder(id);
        try {
            repository.delete(order);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Order cannot be deleted because it has a payment associated with it", exception);
        }
    }

    private Order getOrder(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }
}