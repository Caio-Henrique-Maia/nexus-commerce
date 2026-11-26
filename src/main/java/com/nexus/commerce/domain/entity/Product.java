package com.nexus.commerce.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    // NUMERIC(12,2): até 12 dígitos no total, com 2 casas decimais.
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Estoque inicializado em zero para novas instâncias Java.
    // A restrição no banco também impede valores negativos.
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
}