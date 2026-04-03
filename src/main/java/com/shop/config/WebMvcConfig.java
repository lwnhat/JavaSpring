package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Spring MVC
 * Đăng ký thư mục upload ảnh như là static resource
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * CORS cho REST /api/** (Postman, trình duyệt; app Android không bắt buộc nhưng an toàn khi test).
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*");
    }

    /**
     * Map URL /uploads/** đến thư mục uploads/ trên disk
     * Giúp trình duyệt có thể truy cập ảnh upload trực tiếp
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Phục vụ ảnh upload từ thư mục uploads/products/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
