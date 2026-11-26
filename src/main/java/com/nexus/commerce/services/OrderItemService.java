package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.OrderItem;
import com.nexus.commerce.domain.entity.Product;
import com.nexus.commerce.dto.OrderItemRequest;
import com.nexus.commerce.dto.OrderItemResponse;
import com.nexus.commerce.dto.OrderItemUpdateRequest;
import com.nexus.commerce.mappers.OrderItemMapper;
import com.nexus.commerce.repositories.OrderItemRepository;
import com.nexus.commerce.repositories.OrderRepository;
import com.nexus.commerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemService {
    private final OrderItemRepository repository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper mapper;

    public List<OrderItemResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public OrderItemResponse findById(Long id) {
        return mapper.toResponse(getOrderItem(id));
    }

    @Transactional
    public OrderItemResponse create(OrderItemRequest request) {
        Order order = getOrder(request.orderId());
        Product product = getProduct(request.productId());

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(request.quantity());
        // Mantém o preço vigente como snapshot histórico do item.
        orderItem.setUnitPrice(product.getPrice());

        order.getItems().add(orderItem);
        updateOrderTotal(order);
        return mapper.toResponse(repository.save(orderItem));
    }

    @Transactional
    public OrderItemResponse update(Long id, OrderItemUpdateRequest request) {
        OrderItem orderItem = getOrderItem(id);
        orderItem.setQuantity(request.quantity());
        updateOrderTotal(orderItem.getOrder());
        return mapper.toResponse(repository.save(orderItem));
    }

    @Transactional
    public void delete(Long id) {
        OrderItem orderItem = getOrderItem(id);
        Order order = orderItem.getOrder();
        order.getItems().remove(orderItem);
        updateOrderTotal(order);
    }

    private void updateOrderTotal(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }

    private OrderItem getOrderItem(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}