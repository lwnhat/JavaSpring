package com.shop.controller;

import com.shop.model.Order;
import com.shop.model.User;
import com.shop.service.OrderService;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý đặt hàng và lịch sử đơn hàng
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * Helper: Lấy User từ Authentication
     */
    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    /**
     * Hiển thị trang checkout (thanh toán)
     */
    @GetMapping("/checkout")
    public String checkout(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        // Hiển thị thông tin giao hàng mặc định từ profile user
        model.addAttribute("user", user);
        return "orders/checkout";
    }

    /**
     * Xử lý đặt hàng
     */
    @PostMapping("/place")
    public String placeOrder(
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam(required = false) String note,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(auth);
        try {
            Order order = orderService.createOrder(user, address, phone, note);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đặt hàng thành công! Mã đơn: #" + order.getId());
            return "redirect:/orders/bill/" + order.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    /**
     * Hiển thị lịch sử đơn hàng
     */
    @GetMapping("/history")
    public String orderHistory(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("orders", orderService.getOrdersByUser(user));
        return "orders/history";
    }

    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        // Kiểm tra đơn hàng có thuộc về user hiện tại không
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders/history";
        }

        model.addAttribute("order", order);
        return "orders/detail";
    }

    /**
     * Hiển thị hóa đơn sau khi đặt hàng thành công hoặc khi xem lại
     */
    @GetMapping("/bill/{id}")
    public String bill(@PathVariable Long id, Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders/history";
        }

        model.addAttribute("order", order);
        return "orders/bill";
    }
}
