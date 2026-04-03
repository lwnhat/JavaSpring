package com.shop.controller;

import com.shop.model.Product;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller xử lý trang chủ
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * Hiển thị trang chủ
     * - Danh sách sản phẩm nổi bật
     * - Danh sách sản phẩm mới nhất
     * - Danh sách danh mục
     */
    @GetMapping({"", "/", "/home"})
    public String home(Model model) {
        // Lấy sản phẩm nổi bật (featured)
        List<Product> featuredProducts = productService.findFeatured();
        // Lấy sản phẩm mới nhất
        List<Product> newestProducts = productService.findNewest();
        // Lấy tất cả danh mục
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("newestProducts", newestProducts);
        return "index"; // resources/templates/index.html
    }
}
