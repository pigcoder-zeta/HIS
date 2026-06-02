package com.smarthealthcare.security;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行公开接口
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                // AI导诊接口患者可访问
                .requestMatchers("/api/v1/ai/triage/**").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR",
                        "ROLE_PHARMACIST", "ROLE_MEDICAL_ADMIN", "ROLE_SYSTEM_ADMIN")
                // 患者接口（管理员也可管理）
                .requestMatchers("/api/v1/patient/**").hasAnyAuthority("ROLE_PATIENT", "ROLE_SYSTEM_ADMIN")
                // 医生接口（管理员也可管理）
                .requestMatchers("/api/v1/doctor/**").hasAnyAuthority("ROLE_DOCTOR", "ROLE_SYSTEM_ADMIN")
                // 药房接口（管理员也可管理）
                .requestMatchers("/api/v1/pharmacy/**").hasAnyAuthority("ROLE_PHARMACIST", "ROLE_SYSTEM_ADMIN")
                // 医务科接口（管理员也可管理）
                .requestMatchers("/api/v1/medical/**").hasAnyAuthority("ROLE_MEDICAL_ADMIN", "ROLE_SYSTEM_ADMIN")
                // 管理员接口
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_SYSTEM_ADMIN")
                // 通用接口（需登录）
                .requestMatchers("/api/v1/common/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
