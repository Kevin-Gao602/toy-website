package org.example.toywebsitebackend.config;

import org.example.toywebsitebackend.security.JwtAuthenticationEntryPoint;
import org.example.toywebsitebackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用JWT，不需要CSRF保护）
            .csrf(AbstractHttpConfigurer::disable)
            
            // Enable CORS inside Spring Security so 401/403 responses also include CORS headers.
            .cors(Customizer.withDefaults())
            
            // 会话管理：无状态（使用JWT）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 未登录/无效 token 时的统一返回
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            
            // 请求授权配置
            .authorizeRequests(auth -> auth
                // 预检请求放行
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 登录/注册放行
                .antMatchers("/api/auth/login", "/api/auth/register").permitAll()

                // 产品与测试接口放行（可按需调整）
                .antMatchers("/api/products/**", "/api/test/**").permitAll()

                // 其他接口默认需要登录（包括 /api/auth/me）
                .anyRequest().authenticated()
            )

            // JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

