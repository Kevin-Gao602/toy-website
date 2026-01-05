package org.example.toywebsitebackend.config;

import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.model.enums.Role;
import org.example.toywebsitebackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seed an admin user for local/dev usage.
 * Email: admin@toy.com
 * Password: 123456
 */
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "admin@toy.com";
        if (userRepository.existsByEmail(email)) return;

        User admin = new User();
        admin.setEmail(email);
        admin.setName("Admin");
        admin.setRole(Role.ADMIN);
        admin.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(admin);
    }
}


