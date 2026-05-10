package com.smakroom.order.repository;

import com.smakroom.order.entity.Order;
import com.smakroom.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    // JPQL: order with items eagerly (avoid N+1)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // JPQL: orders for user with items
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product " +
           "WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    // JPQL: count orders by status
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();

    // JPQL with subquery — top customers by total spend
    @Query("SELECT o.user, SUM(o.totalPrice) AS total FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "GROUP BY o.user ORDER BY total DESC")
    List<Object[]> findTopCustomers(Pageable pageable);
}
