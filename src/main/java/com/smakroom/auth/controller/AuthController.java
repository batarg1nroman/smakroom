package com.smakroom.auth.controller;

import com.smakroom.exception.BusinessException;
import com.smakroom.user.dto.RegisterDto;
import com.smakroom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Неверное имя пользователя или пароль");
        }
        if (logout != null) {
            model.addAttribute("successMsg", "Вы вышли из системы");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("successMsg", "Регистрация прошла успешно! Войдите в систему.");
            return "redirect:/auth/login";
        } catch (BusinessException ex) {
            bindingResult.reject("register.error", ex.getMessage());
            return "auth/register";
        }
    }
}
