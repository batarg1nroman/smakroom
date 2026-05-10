package com.smakroom.home.controller;

import com.smakroom.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts",
                productService.findAvailable(PageRequest.of(0, 8)).getContent());
        model.addAttribute("categories",
                productService.findAllCategories());
        return "index";
    }
}
