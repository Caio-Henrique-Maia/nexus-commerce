package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.OrderItem;
import com.nexus.commerce.domain.entity.Product;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.dto.OrderRequest;
import com.nexus.commerce.dto.OrderResponse;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.mappers.OrderMapper;
import com.nexus.commerce.repositories.CustomerRepository;
import com.nexus.commerce.repositories.OrderItemRepository;
import com.nexus.commerce.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository repository;
    private final CustomerRepository customerRepository;
    private final OrderMapper mapper;
    private final ProductService productService;
    private final OrderItemRepository orderItemRepository;

    public Order findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public List<OrderResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public OrderResponse findById(Long id) {
        return mapper.toResponse(getOrder(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setCustomer(getCustomer(request.customerId()));
        order.setOrderStatus(OrderStatus.CREATED);
        repository.save(order);

        for (OrderRequest.OrderRequestItens itens : request.itens()) {
            Product requestProd = productService.getProduct(itens.productId());

            if (!productService.hasStock(requestProd, itens.quantity()) || !productService.hasPositivePrice(requestProd)) {
                throw new IllegalStateException("Product : " + requestProd.getId() + " not available");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(requestProd);
            item.setQuantity(itens.quantity());
            item.setUnitPrice(requestProd.getPrice());
            orderItemRepository.save(item);
            BigDecimal itemSubtotal = requestProd.getPrice().multiply(BigDecimal.valueOf(itens.quantity()));
            totalAmount.add(itemSubtotal);
        }
        order.setTotalAmount(totalAmount);
        return mapper.toResponse(order);
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
            throw new DataIntegrityViolationException("Order cannot be deleted because it has a payment associated with it", exception);
        }
    }

    private Order getOrder(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Order not found"));
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Customer not found"));
    }
}