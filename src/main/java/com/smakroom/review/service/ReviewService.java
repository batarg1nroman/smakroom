package com.smakroom.review.service;

import com.smakroom.exception.BusinessException;
import com.smakroom.exception.ResourceNotFoundException;
import com.smakroom.product.entity.Product;
import com.smakroom.product.repository.ProductRepository;
import com.smakroom.review.dto.ReviewDto;
import com.smakroom.review.entity.Review;
import com.smakroom.review.repository.ReviewRepository;
import com.smakroom.user.entity.User;
import com.smakroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Review addReview(String username, ReviewDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Продукт не найден"));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new BusinessException("Вы уже оставили отзыв на этот продукт");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Review saved = reviewRepository.save(review);
        log.info("Review added: user={}, product={}, rating={}", username, product.getId(), dto.getRating());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Review> findByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    @Transactional
    public void delete(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));

        boolean isOwner = review.getUser().getUsername().equals(username);
        boolean isAdmin = userRepository.findByUsername(username)
                .map(u -> u.getRole().name().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("Нет прав для удаления отзыва");
        }

        reviewRepository.delete(review);
        log.info("Review {} deleted by {}", reviewId, username);
    }
}
