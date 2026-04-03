package com.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lớp khởi chạy ứng dụng Spring Boot
 * Website Bán Quần Áo - Đồ Án Đại Học
 */
@SpringBootApplication
public class ClothingShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClothingShopApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  CLOTHING SHOP đã khởi động thành công!");
        System.out.println("  Truy cập: http://localhost:8080");
        System.out.println("  Admin:    http://localhost:8080/admin");
        System.out.println("===========================================");
    }
}
