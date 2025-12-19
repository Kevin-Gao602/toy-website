package org.example.toywebsitebackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用于生成BCrypt密码的工具类
 * 运行此main方法可以生成加密后的密码
 * 
 * 使用方法：
 * 1. 运行此类的main方法
 * 2. 复制输出的加密字符串到data.sql中
 */
public class BCryptPasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        // 生成常用密码的BCrypt哈希
        System.out.println("=== BCrypt Password Generator ===");
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("user123: " + encoder.encode("user123"));
        System.out.println("password: " + encoder.encode("password"));
        
        // 如果需要生成其他密码，取消下面的注释并修改
        // String password = "your-password-here";
        // System.out.println(password + ": " + encoder.encode(password));
    }
}

