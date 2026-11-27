package com.nexus.commerce.services;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.domain.entity.Order;
import com.nexus.commerce.domain.entity.OrderItem;
import com.nexus.commerce.domain.entity.Product;
import com.nexus.commerce.domain.enums.OrderStatus;
import com.nexus.commerce.dto.OrderCustomerUpdateRequest;
import com.nexus.commerce.dto.OrderRequest;
import com.nexus.commerce.dto.OrderResponse;
import com.nexus.commerce.exceptions.ResourceNotFoundException;
import com.nexus.commerce.mappers.OrderMapper;
import com.nexus.commerce.repositories.CustomerRepository;
import com.nexus.commerce.repositories.OrderRepository;
import com.nexus.commerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository repository;
    private final CustomerRepository customerRepository;
    private final OrderMapper mapper;
    private final ProductService productService;
    private final ProductRepository productRepository;

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
        Map<Long, Integer> requestedQuantities = aggregateQuantities(request.itens());
        Order order = new Order();
        order.setCustomer(getCustomer(request.customerId()));
        order.setOrderStatus(OrderStatus.CREATED);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productService.getProduct(entry.getKey());
            Integer quantity = entry.getValue();

            int updatedRows = productRepository.reserveStock(product.getId(), quantity);

            if (updatedRows == 0 || productService.hasPositivePrice(product)) {
                throw new IllegalStateException("Product " + product.getId() + " unavailable");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemSubtotal);
        }

        order.setTotalAmount(totalAmount);
        return mapper.toResponse(repository.save(order));
    }

    private Map<Long, Integer> aggregateQuantities(List<OrderRequest.OrderRequestItens> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderRequest.OrderRequestItens item : items) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantities;
    }

    private Order getOrder(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Order not found"));
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public OrderResponse updateCustomer(Long id, OrderCustomerUpdateRequest request) {
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
}