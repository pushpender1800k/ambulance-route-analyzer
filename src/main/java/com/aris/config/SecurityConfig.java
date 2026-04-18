package com.aris.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/dashboard/stats").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/login", "/", "/dashboard", "/units", "/hospitals", "/incidents", "/analytics", "/patient", "/driver", "/settings").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                .requestMatchers("/api/admin/**").hasAnyRole("COMMAND", "COORDINATOR", "SUPERVISOR")
                
                .requestMatchers("/api/patient/**").hasRole("PATIENT")
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                
                .requestMatchers("/api/dispatch").hasAnyRole("COMMAND", "DISPATCHER", "COORDINATOR", "SUPERVISOR")
                .requestMatchers("/api/incidents/**").hasAnyRole("COMMAND", "DISPATCHER", "COORDINATOR", "SUPERVISOR", "DRIVER")
                .requestMatchers("/api/hospitals/**").authenticated()
                .requestMatchers("/api/ambulances/**").authenticated()
                .requestMatchers("/api/route/**").authenticated()
                .requestMatchers("/api/events/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
