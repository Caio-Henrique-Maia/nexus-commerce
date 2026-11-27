package com.nexus.commerce.repositories;

import com.nexus.commerce.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("""
            UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity
                        WHERE p.id = :productId
                        AND p.stockQuantity >= :quantity
            """)
    int reserveStock(@Param("235761 ")Long productId, @Param("quantity")Integer quantity);
}