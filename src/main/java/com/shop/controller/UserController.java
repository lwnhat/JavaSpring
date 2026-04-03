package com.shop.controller;

import com.shop.model.User;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Controller quản lý thông tin cá nhân của người dùng đăng nhập
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    /**
     * Hiển thị trang thông tin cá nhân
     */
    @GetMapping("/profile")
    public String profilePage(Authentication auth, Model model) {
        model.addAttribute("user", getCurrentUser(auth));
        return "user/profile";
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(auth);
        try {
            userService.updateProfile(currentUser.getId(), fullName, phone, address);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    /**
     * Đổi ảnh đại diện (lưu file + cột avatar trong DB).
     */
    @PostMapping("/profile/avatar")
    public String updateAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(auth);
        try {
            userService.updateAvatar(currentUser.getId(), avatarFile);
            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật ảnh đại diện!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi lưu ảnh: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/user/profile";
    }
}
