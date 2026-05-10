package com.smakroom.product.controller;

import com.smakroom.product.dto.ProductDto;
import com.smakroom.product.entity.Product;
import com.smakroom.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;

/**
 * REST API for products — full CRUD, accessible by ADMIN only.
 * Used by the admin panel via AJAX.
 */
@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<Page<Product>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available) {

        Page<Product> products = productService.findWithFilters(
                name, categoryId, minPrice, maxPrice, available,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Product> create(@Valid @ModelAttribute ProductDto dto,
                                           @RequestParam(required = false) MultipartFile image) throws IOException {
        Product created = productService.create(dto, image, uploadDir);
        return ResponseEntity.created(URI.create("/api/products/" + created.getId())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> update(@PathVariable Long id,
                                           @Valid @ModelAttribute ProductDto dto,
                                           @RequestParam(required = false) MultipartFile image) throws IOException {
        Product updated = productService.update(id, dto, image, uploadDir);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
