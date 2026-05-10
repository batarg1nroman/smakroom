package com.smakroom.user.controller;

import com.smakroom.user.dto.ProfileDto;
import com.smakroom.user.entity.User;
import com.smakroom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        ProfileDto dto = new ProfileDto();
        if (user.getProfile() != null) {
            dto.setFirstName(user.getProfile().getFirstName());
            dto.setLastName(user.getProfile().getLastName());
            dto.setPhone(user.getProfile().getPhone());
        }
        model.addAttribute("user", user);
        model.addAttribute("profileDto", dto);
        return "user/profile";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute ProfileDto profileDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            return "user/profile";
        }
        userService.updateProfile(userDetails.getUsername(), profileDto);
        redirectAttributes.addFlashAttribute("successMsg", "Профиль обновлён");
        return "redirect:/profile";
    }
}
