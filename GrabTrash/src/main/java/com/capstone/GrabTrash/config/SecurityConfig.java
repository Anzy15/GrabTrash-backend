package com.capstone.GrabTrash.config;
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
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

//import com.capstone.EcoTrack.config.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register", "/api/users/login", "/api/users/security-questions",
                               "/api/users/forgot-password/reset", "/api/users/forgot-password/question",
                               "/api/pickup-locations", "api/barangays").permitAll()
                // Allow unrestricted access to notification test endpoints
                .requestMatchers("/api/notifications/send", "/api/notifications/send-flexible",
                               "/api/notifications/test-send", "/api/notifications/test-barangay", 
                               "/api/notifications/test-role", "/api/notifications/test-collection-reminders",
                               "/api/notifications/test-token-validity", "/api/notifications/send-compat").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pickup-locations/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pickup-locations").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/pickup-locations/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/pickup-locations/**").authenticated()
                // Require authentication for payment endpoints
                .requestMatchers("/api/payments/**").authenticated()
                // Allow all authenticated users to GET trucks
                .requestMatchers(HttpMethod.GET, "/api/trucks").authenticated()
                // Require ADMIN role for other truck management endpoints
                .requestMatchers("/api/trucks/**").hasRole("ADMIN")
                // Allow both ADMIN and PRIVATE_ENTITY roles to update location
                .requestMatchers("/api/users/location").hasAnyRole("ADMIN", "PRIVATE_ENTITY")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
