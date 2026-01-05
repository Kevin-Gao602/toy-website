package org.example.toywebsitebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ToyWebsiteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToyWebsiteBackendApplication.class, args);
    }

}
