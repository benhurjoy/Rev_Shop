package com.revshop.repository;

import com.revshop.entity.Order;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByBuyer(User buyer);
    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o")
    Long countTotalOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    Double sumTotalRevenue();

    // ── Use these wherever order details / items are rendered in templates ──

    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        LEFT JOIN FETCH o.buyer
        WHERE o.id = :id
        """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        WHERE o.buyer = :buyer
        ORDER BY o.orderedAt DESC
        """)
    List<Order> findByBuyerWithItems(@Param("buyer") User buyer);

    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        LEFT JOIN FETCH o.buyer
        ORDER BY o.orderedAt DESC
        """)
    List<Order> findAllWithDetails();
}
