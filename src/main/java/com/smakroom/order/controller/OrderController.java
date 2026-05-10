package com.smakroom.order.controller;

import com.smakroom.exception.BusinessException;
import com.smakroom.order.dto.CheckoutDto;
import com.smakroom.order.entity.Order;
import com.smakroom.order.service.CartService;
import com.smakroom.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        var cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cartService.getTotal(session));
        model.addAttribute("checkoutDto", new CheckoutDto());
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@Valid @ModelAttribute CheckoutDto dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartItems", cartService.getCart(session));
            model.addAttribute("total", cartService.getTotal(session));
            return "order/checkout";
        }

        try {
            Order order = orderService.placeOrder(userDetails.getUsername(), dto.getNotes(), session);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Заказ #" + order.getId() + " успешно оформлен! Ждём вас по адресу: г. Казань, Проезд Яраткан 3А, подъезд 3.");
            return "redirect:/orders/history";
        } catch (BusinessException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            model.addAttribute("cartItems", cartService.getCart(session));
            model.addAttribute("total", cartService.getTotal(session));
            return "order/checkout";
        }
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {

        // Get user's orders with pagination
        Page<Order> orders = orderService.findByUser(
                userService(userDetails.getUsername()),
                PageRequest.of(page, 10, Sort.by("createdAt").descending())
        );
        model.addAttribute("orders", orders);
        return "order/history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Order order = orderService.findById(id);
        // Security: only owner or admin can view
        if (!order.getUser().getUsername().equals(userDetails.getUsername()) &&
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/orders/history";
        }
        model.addAttribute("order", order);
        return "order/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.cancel(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Заказ отменён");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    // Helper: get user id by username (injected via OrderService)
    private Long userService(String username) {
        // We delegate this to orderService to keep service layer clean
        return orderService.findUserIdByUsername(username);
    }
}
