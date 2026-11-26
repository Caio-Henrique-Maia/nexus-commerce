package com.nexus.commerce.repositories;

import com.nexus.commerce.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}