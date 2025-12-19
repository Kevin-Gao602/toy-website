package org.example.toywebsitebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS配置类
 * 允许前端（Vue）跨域访问后端API
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源（前端地址）
        // Vue开发服务器默认端口：5173 (Vite) 或 8080/3000 (其他)
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // Vite默认端口
            "http://localhost:3000",  // 其他前端框架
            "http://localhost:8080",  // 如果前端也用8080
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
        ));
        
        // 允许的HTTP方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 允许发送凭证（cookies, authorization headers等）
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

