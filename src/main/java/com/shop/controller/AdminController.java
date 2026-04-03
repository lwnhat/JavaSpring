package com.shop.controller;

import com.shop.model.*;
import com.shop.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Controller quản lý trang Admin
 * Chỉ ROLE_ADMIN mới được truy cập
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;

    // ==========================================
    // DASHBOARD
    // ==========================================

    /**
     * Trang tổng quan Admin - hiển thị thống kê
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("totalOrders", orderService.countOrders());
        model.addAttribute("totalRevenue", orderService.calculateTotalRevenue());
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("pendingOrders", orderService.countByStatus(Order.OrderStatus.PENDING));
        model.addAttribute("recentOrders", orderService.findAll().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ==========================================
    // PRODUCT MANAGEMENT
    // ==========================================

    /** Danh sách sản phẩm đang bán (ẩn sản phẩm đã xóa mềm) */
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAllActiveForAdmin());
        return "admin/products/list";
    }

    /** Form thêm sản phẩm mới */
    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/products/form";
    }

    /** Form sửa sản phẩm */
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/products/form";
    }

    /** Lưu sản phẩm (thêm mới và cập nhật) */
    @PostMapping("/products/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        // Gán danh mục cho sản phẩm
        if (categoryId != null) {
            categoryService.findById(categoryId).ifPresent(product::setCategory);
        }

        try {
            productService.save(product, imageFile);
            redirectAttributes.addFlashAttribute("successMsg",
                    product.getId() == null ? "Thêm sản phẩm thành công!" : "Cập nhật sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi upload ảnh: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /** Xóa sản phẩm (soft delete) */
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "Đã xóa sản phẩm!");
        return "redirect:/admin/products";
    }

    // ==========================================
    // CATEGORY MANAGEMENT
    // ==========================================

    /** Danh sách danh mục */
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    /** Form thêm danh mục */
    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    /** Form sửa danh mục */
    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    /** Lưu danh mục */
    @PostMapping("/categories/save")
    public String saveCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMsg", "Lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    /** Xóa danh mục */
    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa danh mục!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Không thể xóa danh mục đang có sản phẩm!");
        }
        return "redirect:/admin/categories";
    }

    // ==========================================
    // ORDER MANAGEMENT
    // ==========================================

    /** Danh sách đơn hàng */
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/orders/list";
    }

    /** Chi tiết đơn hàng */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        model.addAttribute("order", order);
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/orders/detail";
    }

    /** Cập nhật trạng thái đơn hàng */
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status,
            RedirectAttributes redirectAttributes) {

        orderService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật trạng thái đơn hàng!");
        return "redirect:/admin/orders/" + id;
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    /** Danh sách users */
    @GetMapping("/users")
    public String listUsers(Model model) {
        java.util.List<com.shop.model.User> users = userService.findAll();
        long adminCount = users.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        long userCount  = users.stream().filter(u -> "ROLE_USER".equals(u.getRole())).count();
        model.addAttribute("users", users);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("userCount", userCount);
        return "admin/users/list";
    }
}
