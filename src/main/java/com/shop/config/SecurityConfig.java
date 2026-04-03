package com.shop.config;

import com.shop.security.JwtAuthenticationFilter;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Cấu hình Spring Security
 * Phân quyền truy cập theo role: ROLE_USER và ROLE_ADMIN
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Cấu hình Authentication Provider
     * Kết nối UserDetailsService (UserService) với PasswordEncoder
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Bean AuthenticationManager cho REST API login
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình Security Filter Chain
     * Định nghĩa các URL nào cần xác thực, URL nào được phép truy cập tự do
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            // Cấu hình phân quyền URL
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập tự do (public)
                .requestMatchers(
                    "/",
                    "/home",
                    "/products",
                    "/products/**",
                    "/categories/**",
                    "/search",
                    "/auth/**",
                    "/register",
                    "/login",
                    "/error",
                    "/error/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/uploads/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()
                // API công khai cho app Android (không gồm /api/admin)
                .requestMatchers(
                    "/api/health",
                    "/api/products",
                    "/api/products/**",
                    "/api/categories",
                    "/api/login",
                    "/api/register",
                    "/api/profile/avatar",
                    "/api/orders",
                    "/api/orders/user/**",
                    "/api/cart/**"
                ).permitAll()
                // Chi tiết đơn theo id (app Android có thể gọi khi backend expose)
                .requestMatchers(HttpMethod.GET, "/api/orders/*").permitAll()
                // REST admin: JWT Bearer + ROLE_ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Chỉ ADMIN mới vào được /admin/** (Thymeleaf)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Các URL còn lại cần đăng nhập
                .anyRequest().authenticated()
            )

            // Cấu hình form đăng nhập
            .formLogin(form -> form
                .loginPage("/auth/login")           // Trang login tùy chỉnh
                .loginProcessingUrl("/auth/login")  // URL xử lý form submit
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)        // Chuyển về home sau khi login thành công
                .failureUrl("/auth/login?error=true") // Khi login thất bại
                .permitAll()
            )

            // Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Cấu hình cho REST API (không dùng csrf cho API)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
