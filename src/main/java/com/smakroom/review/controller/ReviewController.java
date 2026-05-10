package com.smakroom.review.controller;

import com.smakroom.exception.BusinessException;
import com.smakroom.review.dto.ReviewDto;
import com.smakroom.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReview(
            @Valid @RequestBody ReviewDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        try {
            var review = reviewService.addReview(userDetails.getUsername(), dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Отзыв добавлен",
                    "reviewId", review.getId(),
                    "rating", review.getRating()
            ));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            reviewService.delete(id, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Отзыв удалён"));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
