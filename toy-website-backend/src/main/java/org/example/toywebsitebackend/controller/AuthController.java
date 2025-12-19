package org.example.toywebsitebackend.controller;

import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证Controller
 * 提供用户注册、登录等认证相关的API接口
 * 
 * 注意：当前版本为简化版本，未实现JWT token
 * 后续需要添加JWT支持
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        // 构建响应（暂时不返回token，后续需要添加JWT）
        Map<String, Object> response = new HashMap<>();
        response.put("message", "注册成功");
        response.put("user", Map.of(
            "id", savedUser.getId(),
            "email", savedUser.getEmail(),
            "name", savedUser.getName(),
            "role", savedUser.getRole().toString()
        ));
        // TODO: 添加JWT token
        response.put("token", "NOT_IMPLEMENTED_YET");

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

        // 构建响应（暂时不返回token，后续需要添加JWT）
        Map<String, Object> response = new HashMap<>();
        response.put("message", "登录成功");
        response.put("user", Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "name", user.getName(),
            "role", user.getRole().toString()
        ));
        // TODO: 添加JWT token
        response.put("token", "NOT_IMPLEMENTED_YET");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/me
     * 
     * 注意：当前版本未实现JWT验证，返回占位符响应
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // TODO: 实现JWT token验证
        Map<String, Object> error = new HashMap<>();
        error.put("error", "JWT认证未实现，请先实现JWT token验证");
        error.put("message", "此接口需要JWT token验证，当前版本暂未实现");
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
    }
}

