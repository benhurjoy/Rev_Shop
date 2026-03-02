package com.revshop.repository;

import com.revshop.entity.Product;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller(User seller);
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueOrderByCreatedAtDesc();
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findBySellerAndActiveTrue(User seller);
    List<Product> findByStockQuantityLessThan(int threshold);
    List<Product> findByStockQuantityEquals(int quantity);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller = :seller AND p.active = true")
    Long countActiveProductsBySeller(User seller);
}
