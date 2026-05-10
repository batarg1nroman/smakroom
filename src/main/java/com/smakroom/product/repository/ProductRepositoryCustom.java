package com.smakroom.product.repository;

import com.smakroom.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductRepositoryCustom {

    Page<Product> findWithFilters(String name, Long categoryId,
                                  BigDecimal minPrice, BigDecimal maxPrice,
                                  Boolean available, Pageable pageable);
}
