package com.revshop.repository;

import com.revshop.entity.Order;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerOrderByOrderedAtDesc(User buyer);
    List<Order> findAllByOrderByOrderedAtDesc();
    List<Order> findByStatus(Order.OrderStatus status);
    long countByBuyer(User buyer);

    @Query("SELECT COUNT(o) FROM Order o")
    Long countTotalOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    Double sumTotalRevenue();
}

