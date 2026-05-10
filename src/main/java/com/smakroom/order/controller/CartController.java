package com.smakroom.order.controller;

import com.smakroom.order.service.CartService;
import com.smakroom.product.entity.Product;
import com.smakroom.product.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping
    public String cart(HttpSession session, Model model) {
        model.addAttribute("cartItems", cartService.getCart(session));
        model.addAttribute("total", cartService.getTotal(session));
        return "order/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {

        Product product = productService.findById(productId);
        cartService.addToCart(session, product, quantity);
        return ResponseEntity.ok(Map.of(
                "count", cartService.getItemCount(session),
                "total", cartService.getTotal(session),
                "message", "Добавлено в корзину"
        ));
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam Long productId,
            @RequestParam int quantity,
            HttpSession session) {

        cartService.updateQuantity(session, productId, quantity);
        return ResponseEntity.ok(Map.of(
                "count", cartService.getItemCount(session),
                "total", cartService.getTotal(session),
                "itemTotal", cartService.getCart(session).stream()
                        .filter(i -> i.getProductId().equals(productId))
                        .findFirst()
                        .map(i -> i.getSubtotal().toString())
                        .orElse("0")
        ));
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeItem(
            @RequestParam Long productId,
            HttpSession session) {

        cartService.removeFromCart(session, productId);
        return ResponseEntity.ok(Map.of(
                "count", cartService.getItemCount(session),
                "total", cartService.getTotal(session),
                "message", "Товар удалён"
        ));
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        cartService.clearCart(session);
        return ResponseEntity.ok(Map.of("message", "Корзина очищена"));
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cartCount(HttpSession session) {
        return ResponseEntity.ok(Map.of("count", cartService.getItemCount(session)));
    }
}
