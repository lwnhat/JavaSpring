package com.shop.controller;

import com.shop.model.Product;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý hiển thị sản phẩm cho người dùng
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * Hiển thị danh sách sản phẩm với tìm kiếm và phân trang
     * @param keyword  từ khóa tìm kiếm (tùy chọn)
     * @param categoryId ID danh mục (tùy chọn)
     * @param page     số trang (bắt đầu từ 0)
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Mỗi trang hiển thị 12 sản phẩm
        Page<Product> productPage = productService.searchProducts(keyword, categoryId, page, 12);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findAll());

        return "products/list";
    }

    /**
     * Hiển thị chi tiết sản phẩm
     * @param id ID của sản phẩm
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        model.addAttribute("product", product);
        // Gợi ý sản phẩm cùng danh mục
        if (product.getCategory() != null) {
            model.addAttribute("relatedProducts",
                    productService.findByCategory(product.getCategory())
                            .stream()
                            .filter(p -> !p.getId().equals(id))
                            .limit(4)
                            .toList());
        }
        return "products/detail";
    }
}
