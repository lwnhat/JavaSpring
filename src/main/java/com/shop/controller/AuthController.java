package com.shop.controller;

import com.shop.model.User;
import com.shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý đăng ký và đăng nhập
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Hiển thị trang đăng nhập
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Bạn đã đăng xuất thành công!");
        }
        return "auth/login";
    }

    /**
     * Hiển thị trang đăng ký
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * Xử lý form đăng ký
     * @param user  đối tượng từ form
     * @param confirmPassword mật khẩu xác nhận
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra lỗi validation từ annotations
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Kiểm tra mật khẩu xác nhận có khớp không
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordError", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
        }

        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }
}
