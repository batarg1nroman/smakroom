package com.smakroom.product.controller;

import com.smakroom.currency.service.CurrencyService;
import com.smakroom.product.entity.Category;
import com.smakroom.product.entity.Product;
import com.smakroom.product.service.ProductService;
import com.smakroom.review.dto.ReviewDto;
import com.smakroom.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final CurrencyService currencyService;

    @GetMapping
    public String catalog(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "12") int size,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String query,
                          @RequestParam(required = false) String sort,
                          Model model) {

        Sort sorting = switch (sort != null ? sort : "") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name" -> Sort.by("name").ascending();
            default -> Sort.by("createdAt").descending();
        };

        PageRequest pageable = PageRequest.of(page, size, sorting);
        Page<Product> products;

        if (query != null && !query.isBlank()) {
            products = productService.search(query, pageable);
            model.addAttribute("query", query);
        } else if (categoryId != null) {
            products = productService.findByCategory(categoryId, pageable);
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            products = productService.findAvailable(pageable);
        }

        List<Category> categories = productService.findAllCategories();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("sort", sort);
        model.addAttribute("rates", currencyService.getRates());
        return "product/catalog";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int reviewPage,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        Product product = productService.findById(id);
        var reviews = reviewService.findByProduct(id, PageRequest.of(reviewPage, 5, Sort.by("createdAt").descending()));
        boolean canReview = false;

        if (userDetails != null) {
            canReview = !reviews.getContent().stream()
                    .anyMatch(r -> r.getUser().getUsername().equals(userDetails.getUsername()));
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewDto", new ReviewDto());
        model.addAttribute("canReview", canReview);
        model.addAttribute("rates", currencyService.getRates());
        return "product/detail";
    }
}
