package com.revshop.repository;


import com.revshop.entity.Notification;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUserAndIsReadFalse(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user")
    void markAllAsReadByUser(@Param("user") User user);

    // ── Use these for template rendering ──

    @Query("""
        SELECT n FROM Notification n
        JOIN FETCH n.user
        WHERE n.user = :user
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findByUserWithDetails(@Param("user") User user);

    @Query("""
        SELECT n FROM Notification n
        JOIN FETCH n.user
        WHERE n.user = :user AND n.isRead = false
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findUnreadByUser(@Param("user") User user);
}

