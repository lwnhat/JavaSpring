package com.shop.controller;

import com.shop.model.CartItem;
import com.shop.model.User;
import com.shop.service.CartService;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller xử lý giỏ hàng
 * Yêu cầu người dùng phải đăng nhập
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * Helper: Lấy User từ Authentication
     */
    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    /**
     * Hiển thị giỏ hàng
     */
    @GetMapping
    public String viewCart(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        List<CartItem> cartItems = cartService.getCartItems(user);
        BigDecimal total = cartService.calculateTotal(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "cart/cart";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @GetMapping("/add")
    public String addToCartByGet(Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            redirectAttributes.addFlashAttribute("errorMsg", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            return "redirect:/auth/login";
        }
        redirectAttributes.addFlashAttribute("errorMsg", "Yêu cầu không hợp lệ. Vui lòng thêm sản phẩm từ trang sản phẩm.");
        return "redirect:/products";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(auth);
        try {
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMsg", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cart";
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam Long cartItemId,
            @RequestParam int quantity,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(auth);
        try {
            cartService.updateQuantity(user, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật giỏ hàng!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cart";
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(
            @PathVariable Long cartItemId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(auth);
        try {
            cartService.removeFromCart(user, cartItemId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cart";
    }
}
