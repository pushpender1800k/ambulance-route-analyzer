package com.aris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.aris.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class ArisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArisApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("═══ REGISTERED USERS ═══");
            userRepository.findAll().forEach(u -> 
                System.out.printf("User: %s | Role: %s\n", u.getUsername(), u.getRole())
            );
            System.out.println("HASH for 'admin123': " + passwordEncoder.encode("admin123"));
            System.out.println("════════════════════════");
        };
    }
}
