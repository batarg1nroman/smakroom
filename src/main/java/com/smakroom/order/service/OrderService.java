package com.smakroom.order.service;

import com.smakroom.exception.BusinessException;
import com.smakroom.exception.ResourceNotFoundException;
import com.smakroom.order.dto.CartItemDto;
import com.smakroom.order.entity.Order;
import com.smakroom.order.entity.OrderItem;
import com.smakroom.order.entity.OrderStatus;
import com.smakroom.order.repository.OrderRepository;
import com.smakroom.product.entity.Product;
import com.smakroom.product.repository.ProductRepository;
import com.smakroom.user.entity.User;
import com.smakroom.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Transactional
    public Order placeOrder(String username, String notes, HttpSession session) {
        List<CartItemDto> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            throw new BusinessException("Корзина пуста");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Order order = new Order();
        order.setUser(user);
        order.setNotes(notes);

        for (CartItemDto item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Продукт не найден: " + item.getProductId()));

            if (!product.isAvailable()) {
                throw new BusinessException("Продукт недоступен: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            order.addItem(orderItem);
        }

        order.recalculateTotal();
        Order saved = orderRepository.save(order);
        cartService.clearCart(session);

        log.info("Order placed: id={}, user={}, total={}", saved.getId(), username, saved.getTotalPrice());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Order> findByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Long findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"))
                .getId();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        log.info("Order {} status changed to {}", id, status);
        return saved;
    }

    @Transactional
    public void cancel(Long id, String username) {
        Order order = findById(id);
        if (!order.getUser().getUsername().equals(username)) {
            throw new BusinessException("Нет прав для отмены заказа");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Отменить можно только заказ в статусе 'Ожидает подтверждения'");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled by {}", id, username);
    }
}
