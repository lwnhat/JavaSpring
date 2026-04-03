package com.shop.service;

import com.shop.model.User;
import com.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý logic liên quan đến User
 * Implement UserDetailsService để Spring Security có thể load user
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final String AVATAR_UPLOAD_DIRNAME = "avatars";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Security gọi method này khi user đăng nhập
     * Load user từ database theo username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        // Tạo đối tượng UserDetails với quyền truy cập tương ứng
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                Collections.singleton(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    /**
     * Đăng ký tài khoản mới
     * @param user đối tượng User từ form đăng ký
     * @return User đã được lưu vào database
     */
    @Transactional
    public User register(User user) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER"); // Mặc định là USER
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * Lấy thông tin User theo username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Lấy thông tin User theo ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Lấy tất cả users (cho admin)
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Cập nhật thông tin người dùng
     */
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Cập nhật thông tin hồ sơ cơ bản của user
     */
    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        String normalizedFullName = normalizeNullable(fullName);
        String normalizedPhone = normalizeNullable(phone);
        String normalizedAddress = normalizeNullable(address);

        if (normalizedFullName != null && normalizedFullName.length() > 100) {
            throw new RuntimeException("Họ tên tối đa 100 ký tự!");
        }
        if (normalizedPhone != null && normalizedPhone.length() > 15) {
            throw new RuntimeException("Số điện thoại tối đa 15 ký tự!");
        }
        if (normalizedAddress != null && normalizedAddress.length() > 255) {
            throw new RuntimeException("Địa chỉ tối đa 255 ký tự!");
        }

        user.setFullName(normalizedFullName);
        user.setPhone(normalizedPhone);
        user.setAddress(normalizedAddress);
        return userRepository.save(user);
    }

    /**
     * Lưu ảnh avatar mới; tên file ghi vào cột {@code avatar}, file lưu trong {@code uploads/avatars/}.
     */
    @Transactional
    public User updateAvatar(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn file ảnh!");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Chỉ được tải lên file ảnh (jpg, png, webp, …)!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Path uploadPath = Paths.get("uploads", AVATAR_UPLOAD_DIRNAME);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        deleteAvatarFileIfExists(user.getAvatar());

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : ".jpg";
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            extension = ".jpg";
        }
        String fileName = UUID.randomUUID() + extension;
        Path dest = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        user.setAvatar(fileName);
        return userRepository.save(user);
    }

    private void deleteAvatarFileIfExists(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        if (storedName.startsWith("/") || storedName.startsWith("http")) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get("uploads", AVATAR_UPLOAD_DIRNAME, storedName.trim()));
        } catch (IOException ignored) {
            // bỏ qua nếu file cũ không tồn tại
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Đếm số lượng user (cho dashboard admin)
     */
    public long countUsers() {
        return userRepository.count();
    }
}
