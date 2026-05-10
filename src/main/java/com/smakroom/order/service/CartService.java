package com.smakroom.order.service;

import com.smakroom.order.dto.CartItemDto;
import com.smakroom.product.entity.Product;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartService {

    private static final String CART_KEY = "CART";

    public List<CartItemDto> getCart(HttpSession session) {
        List<CartItemDto> cart = (List<CartItemDto>) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Product product, int quantity) {
        List<CartItemDto> cart = getCart(session);
        Optional<CartItemDto> existing = cart.stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            cart.add(new CartItemDto(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity,
                    product.getImageUrl()
            ));
        }
        session.setAttribute(CART_KEY, cart);
        log.debug("Added product {} to cart, qty={}", product.getId(), quantity);
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        List<CartItemDto> cart = getCart(session);
        if (quantity <= 0) {
            cart.removeIf(i -> i.getProductId().equals(productId));
        } else {
            cart.stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(i -> i.setQuantity(quantity));
        }
        session.setAttribute(CART_KEY, cart);
    }

    public void removeFromCart(HttpSession session, Long productId) {
        List<CartItemDto> cart = getCart(session);
        cart.removeIf(i -> i.getProductId().equals(productId));
        session.setAttribute(CART_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }

    public BigDecimal getTotal(HttpSession session) {
        return getCart(session).stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount(HttpSession session) {
        return getCart(session).stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }
}
