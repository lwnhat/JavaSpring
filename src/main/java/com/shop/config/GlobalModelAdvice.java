package com.shop.config;

import com.shop.model.User;
import com.shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Đưa {@code currentUser} vào model cho Thymeleaf (navbar avatar, v.v.).
 * Bỏ qua {@code /api/**} để không truy vấn DB trên mỗi request REST.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UserService userService;

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api")) {
            return null;
        }
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userService.findByUsername(authentication.getName()).orElse(null);
    }
}
