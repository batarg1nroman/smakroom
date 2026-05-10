package com.smakroom.product.repository;

import com.smakroom.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    Page<Product> findByAvailableTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndAvailableTrue(Long categoryId, Pageable pageable);

    // JPQL: search by name or description (case-insensitive)
    @Query("SELECT p FROM Product p WHERE p.available = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchAvailable(@Param("query") String query, Pageable pageable);

    // JPQL: products in price range
    @Query("SELECT p FROM Product p WHERE p.available = true AND p.price BETWEEN :min AND :max")
    Page<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max, Pageable pageable);

    // JPQL with subquery (bonus: complex SQL) — products ordered more than N times
    @Query("SELECT p FROM Product p WHERE p.id IN " +
           "(SELECT oi.product.id FROM OrderItem oi GROUP BY oi.product.id HAVING COUNT(oi) > :minOrders) " +
           "AND p.available = true")
    List<Product> findPopularProducts(@Param("minOrders") long minOrders);

    // JPQL: top products by revenue (complex subquery for bonus points)
    @Query("SELECT p, SUM(oi.quantity * oi.price) AS revenue FROM Product p " +
           "JOIN p.orderItems oi " +
           "JOIN oi.order o WHERE o.status = 'COMPLETED' " +
           "GROUP BY p ORDER BY revenue DESC")
    List<Object[]> findTopProductsByRevenue(Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);
}
