package com.shop.config;

import com.shop.model.User;
import com.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Khởi tạo/tự sửa user dữ liệu nền khi chạy production.
 * - Chuyển password plaintext -> BCrypt nếu phát hiện dữ liệu cũ.
 * - Đảm bảo luôn có tài khoản admin để đăng nhập sau deploy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapUserInitializer implements CommandLineRunner {

    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-users.enabled:true}")
    private boolean bootstrapUsersEnabled;

    @Value("${app.bootstrap-admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap-admin.email:admin@shop.com}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password:admin123}")
    private String adminPassword;

    @Value("${app.bootstrap-admin.force-reset-password:false}")
    private boolean forceResetAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapUsersEnabled) {
            log.info("[BootstrapUser] disabled by app.bootstrap-users.enabled=false");
            return;
        }

        try {
            migratePlaintextPasswords();
            ensureAdminAccount();
        } catch (Exception ex) {
            // Không để lỗi bootstrap user làm ứng dụng fail startup trên cloud.
            log.error("[BootstrapUser] initialization failed, skip bootstrap and continue startup", ex);
        }
    }

    private void migratePlaintextPasswords() {
        List<User> users = userRepository.findAll();
        int changed = 0;

        for (User user : users) {
            boolean dirty = false;

            String password = user.getPassword();
            if (password != null && !password.isBlank() && !isBcryptHash(password)) {
                user.setPassword(passwordEncoder.encode(password));
                dirty = true;
            }

            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("ROLE_USER");
                dirty = true;
            }

            if (!user.isEnabled()) {
                user.setEnabled(true);
                dirty = true;
            }

            if (dirty) {
                changed++;
            }
        }

        if (changed > 0) {
            userRepository.saveAll(users);
            log.warn("[BootstrapUser] Migrated {} user records (password/role/enabled)", changed);
        }
    }

    private void ensureAdminAccount() {
        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        if (admin == null) {
            admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(resolveAdminEmail());
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ROLE_ADMIN");
            admin.setFullName("Administrator");
            admin.setEnabled(true);
            userRepository.save(admin);
            log.warn("[BootstrapUser] Created bootstrap admin account: {}", adminUsername);
            return;
        }

        boolean dirty = false;
        if (!"ROLE_ADMIN".equals(admin.getRole())) {
            admin.setRole("ROLE_ADMIN");
            dirty = true;
        }
        if (!admin.isEnabled()) {
            admin.setEnabled(true);
            dirty = true;
        }

        String password = admin.getPassword();
        if (forceResetAdminPassword || password == null || password.isBlank() || !isBcryptHash(password)) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            dirty = true;
            if (forceResetAdminPassword) {
                log.warn("[BootstrapUser] Force reset admin password from app.bootstrap-admin.password");
            } else {
                log.warn("[BootstrapUser] Reset admin password from app.bootstrap-admin.password");
            }
        }

        if (dirty) {
            userRepository.save(admin);
            log.warn("[BootstrapUser] Normalized bootstrap admin account: {}", adminUsername);
        }
    }

    private String resolveAdminEmail() {
        if (!userRepository.existsByEmail(adminEmail)) {
            return adminEmail;
        }
        return adminUsername + "+bootstrap@shop.local";
    }

    private boolean isBcryptHash(String value) {
        return BCRYPT_PATTERN.matcher(value).matches();
    }
}
