package com.smakroom.product.repository;

import com.smakroom.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    // JPQL: categories that have at least one available product
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE p.available = true")
    List<Category> findCategoriesWithAvailableProducts();

    // JPQL: count products per category
    @Query("SELECT c.name, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c.name")
    List<Object[]> countProductsPerCategory();
}
