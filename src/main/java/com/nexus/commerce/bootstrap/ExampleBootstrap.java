package com.nexus.commerce.bootstrap;

import com.nexus.commerce.domain.entity.Customer;
import com.nexus.commerce.domain.entity.Product;
import com.nexus.commerce.repositories.CustomerRepository;
import com.nexus.commerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/// Classe feita com intuito de inicializar alguns exemplos para não ter que ficar fazendo cadastro
@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleBootstrap implements ApplicationRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    private final JdbcTemplate jdbcTemplate;

    private void createCustomer() {
        jdbcTemplate.update(
                "INSERT INTO customers (id, name, email) VALUES (?, ?, ?)",
                100L, "Default customer", "example@example.com");
    }

    private void createProduct() {
        jdbcTemplate.update(
                "INSERT INTO products (id, name, price, stock_quantity) VALUES (?, ?, ?, ?)",
                100L, "Mock product to be used as test", BigDecimal.valueOf(200), 1000);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (customerRepository.existsById(100L)) {
            log.info("Default customer already exists, skipping");
        } else {
            log.info("Default customer not found, creating one with ID 100");
            createCustomer();
        }

        if (productRepository.existsById(100L)) {
            log.info("Default product already exists, skipping");
        } else {
            log.info("Default product not found, creating one with ID 100");
            createProduct();
        }
    }
}
