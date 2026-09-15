package com.thoth.config;

import com.thoth.config.security.JwtAuthenticationFilter;
import com.thoth.config.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/qr/**").permitAll()
                .requestMatchers("/api/v1/alerts/**").authenticated()
                .requestMatchers("/api/v1/sedes/**").permitAll()
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/equipment/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/equipment/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/equipment/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/equipment/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/equipment/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers("/api/v1/maintenance/**").permitAll()
                .requestMatchers("/api/v1/ai/**").authenticated()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}