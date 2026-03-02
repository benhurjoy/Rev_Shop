package com.revshop.repository;

import com.revshop.entity.Product;
import com.revshop.entity.Review;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    Optional<Review> findByProductAndBuyer(Product product, User buyer);
    boolean existsByProductAndBuyer(Product product, User buyer);
    List<Review> findByBuyer(User buyer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(Product product);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    Long countByProduct(Product product);
}
