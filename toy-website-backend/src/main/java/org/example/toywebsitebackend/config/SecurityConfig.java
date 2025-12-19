package org.example.toywebsitebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 * 
 * 注意：此配置是必要的，因为项目中有 spring-boot-starter-security 依赖。
 * 如果没有此配置，Spring Security 默认会要求所有请求都需要认证，导致 API 返回 401。
 * 
 * CORS 配置已由 CorsConfig.java 处理，这里不再重复配置。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用JWT，不需要CSRF保护）
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 配置由 CorsConfig.java 中的 CorsFilter 处理
            
            // 会话管理：无状态（使用JWT）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 请求授权配置
            .authorizeHttpRequests(auth -> auth
                // 暂时允许所有API访问（开发阶段）
                // 后续会改为：
                // - /api/auth/** -> permitAll()
                // - /api/products/** -> permitAll()
                // - /api/cart/**, /api/orders/** -> authenticated()
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}

