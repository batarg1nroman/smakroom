package com.smakroom.admin.controller;

import com.smakroom.order.entity.Order;
import com.smakroom.order.entity.OrderStatus;
import com.smakroom.order.service.OrderService;
import com.smakroom.product.dto.ProductDto;
import com.smakroom.product.entity.Product;
import com.smakroom.product.service.ProductService;
import com.smakroom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // --- Dashboard ---

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.findAll(PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("totalOrders", orderService.findAll(PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("popularProducts", productService.findPopular());
        return "admin/dashboard";
    }

    // --- Products CRUD ---

    @GetMapping("/products")
    public String productList(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> products = productService.findAll(
                PageRequest.of(page, 15, Sort.by("createdAt").descending()));
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.findAllCategories());
        return "admin/products/list";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", productService.findAllCategories());
        return "admin/products/form";
    }

    @PostMapping("/products/new")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) MultipartFile image,
                                 RedirectAttributes redirectAttributes,
                                 Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.findAllCategories());
            return "admin/products/form";
        }
        Product created = productService.create(productDto, image, uploadDir);
        redirectAttributes.addFlashAttribute("successMsg", "Продукт «" + created.getName() + "» добавлен");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setAvailable(product.isAvailable());
        dto.setStock(product.getStock());

        model.addAttribute("productDto", dto);
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.findAllCategories());
        return "admin/products/form";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                 @Valid @ModelAttribute ProductDto productDto,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) MultipartFile image,
                                 RedirectAttributes redirectAttributes,
                                 Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.findAllCategories());
            model.addAttribute("product", productService.findById(id));
            return "admin/products/form";
        }
        Product updated = productService.update(id, productDto, image, uploadDir);
        redirectAttributes.addFlashAttribute("successMsg", "Продукт «" + updated.getName() + "» обновлён");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMsg", "Продукт удалён");
        return "redirect:/admin/products";
    }

    // --- Orders management ---

    @GetMapping("/orders")
    public String orderList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String status,
                            Model model) {
        Page<Order> orders = orderService.findAll(
                PageRequest.of(page, 20, Sort.by("createdAt").descending()));
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/detail";
    }

    @PostMapping("/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            Order order = orderService.updateStatus(id, orderStatus);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", order.getStatus().name(),
                    "statusDisplay", order.getStatus().getDisplayName()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Недопустимый статус"));
        }
    }

    // --- Users ---

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @PostMapping("/users/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return ResponseEntity.ok(Map.of("message", "Статус изменён"));
    }

    // --- Categories ---

    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        var category = productService.createCategory(name, description);
        return ResponseEntity.ok(Map.of(
                "id", category.getId(),
                "name", category.getName()
        ));
    }
}
