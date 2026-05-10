package com.smakroom.product.service;

import com.smakroom.exception.ResourceNotFoundException;
import com.smakroom.product.dto.ProductDto;
import com.smakroom.product.entity.Category;
import com.smakroom.product.entity.Product;
import com.smakroom.product.repository.CategoryRepository;
import com.smakroom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<Product> findAvailable(Pageable pageable) {
        return productRepository.findByAvailableTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndAvailableTrue(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String query, Pageable pageable) {
        return productRepository.searchAvailable(query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findWithFilters(String name, Long categoryId,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Boolean available, Pageable pageable) {
        return productRepository.findWithFilters(name, categoryId, minPrice, maxPrice, available, pageable);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продукт не найден: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product create(ProductDto dto, MultipartFile image, String uploadDir) throws IOException {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

        Product product = new Product();
        fillFromDto(product, dto, category);

        if (image != null && !image.isEmpty()) {
            product.setImageUrl(saveImage(image, uploadDir));
        }

        Product saved = productRepository.save(product);
        log.info("Created product: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductDto dto, MultipartFile image, String uploadDir) throws IOException {
        Product product = findById(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

        fillFromDto(product, dto, category);

        if (image != null && !image.isEmpty()) {
            product.setImageUrl(saveImage(image, uploadDir));
        }

        Product saved = productRepository.save(product);
        log.info("Updated product: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
        log.info("Deleted product: id={}", id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category createCategory(String name, String description) {
        Category category = new Category(name, description);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Product> findPopular() {
        return productRepository.findPopularProducts(2);
    }

    private void fillFromDto(Product product, ProductDto dto, Category category) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setAvailable(dto.isAvailable());
        product.setStock(dto.getStock());
        product.setCategory(category);
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            product.setImageUrl(dto.getImageUrl());
        }
    }

    private String saveImage(MultipartFile image, String uploadDir) throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path path = dir.resolve(filename);
        Files.write(path, image.getBytes());
        return "/uploads/" + filename;
    }
}
