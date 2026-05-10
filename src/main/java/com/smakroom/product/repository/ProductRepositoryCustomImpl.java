package com.smakroom.product.repository;

import com.smakroom.product.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> findWithFilters(String name, Long categoryId,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Boolean available, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Data query
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        root.fetch("category", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, root, name, categoryId, minPrice, maxPrice, available);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("createdAt")));

        List<Product> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, name, categoryId, minPrice, maxPrice, available);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Product> root,
                                             String name, Long categoryId,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             Boolean available) {
        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (available != null) {
            predicates.add(cb.equal(root.get("available"), available));
        }

        return predicates;
    }
}
