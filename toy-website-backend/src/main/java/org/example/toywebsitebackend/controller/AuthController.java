package org.example.toywebsitebackend.controller;

import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.repository.UserRepository;
import org.example.toywebsitebackend.security.CustomUserDetails;
import org.example.toywebsitebackend.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证Controller
 * 提供用户注册、登录等认证相关的API接口
 * 
 * 注意：当前版本已实现 JWT token（HS256）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String name = request.get("name");

        // 参数验证
        if (email == null || email.isEmpty() || 
            password == null || password.isEmpty() || 
            name == null || name.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "参数验证失败：邮箱、密码和姓名都是必填项");
            return ResponseEntity.badRequest().body(error);
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "邮箱已存在");
            return ResponseEntity.badRequest().body(error);
        }

        // 创建新用户
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(org.example.toywebsitebackend.model.enums.Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        // 签发 JWT
        String token = jwtTokenProvider.generateToken(savedUser);

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("message", "注册成功");
        response.put("user", Map.of(
            "id", savedUser.getId(),
            "email", savedUser.getEmail(),
            "name", savedUser.getName(),
            "role", savedUser.getRole().toString()
        ));
        response.put("token", token);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        // 参数验证
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "邮箱和密码都是必填项");
            return ResponseEntity.badRequest().body(error);
        }

        // 查找用户
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "邮箱或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        User user = userOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "邮箱或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // 签发 JWT
        String token = jwtTokenProvider.generateToken(user);

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("message", "登录成功");
        response.put("user", Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "name", user.getName(),
            "role", user.getRole().toString()
        ));
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/me
     * 
     * 需要在请求头中携带 Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByEmail(principal.getUsername());
        if (userOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole().toString());
        return ResponseEntity.ok(response);
    }
}

