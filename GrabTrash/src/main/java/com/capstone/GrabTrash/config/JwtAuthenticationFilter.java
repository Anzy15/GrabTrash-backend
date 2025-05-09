package com.capstone.GrabTrash.config;

import com.capstone.GrabTrash.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/users/register",
        "/api/users/login",
        "/api/users/security-questions",
        "/api/users/forgot-password/reset",
        "/api/users/forgot-password/question",
        "/api/payments",
        "/api/payments/"
    );

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // Allow public paths, GET requests to pickup-locations, and all payment endpoints
        boolean isPublicPath = PUBLIC_PATHS.contains(path);
        boolean isPublicPickupLocation = "GET".equals(method) &&
            (path.equals("/api/pickup-locations") || path.startsWith("/api/pickup-locations/"));
        boolean isPaymentEndpoint = path.startsWith("/api/payments");

        if (isPublicPath || isPublicPickupLocation || isPaymentEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        System.out.println("Auth header: " + (authHeader != null ? "present" : "missing")); // Debug log

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid Bearer token found"); // Debug log
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);
            System.out.println("Extracted email from token: " + userEmail); // Debug log

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("Loaded user details for: " + userEmail); // Debug log

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("Token is valid"); // Debug log
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("Token validation failed"); // Debug log
                }
            }
        } catch (Exception e) {
            // Log the error but don't throw it
            System.err.println("Error processing JWT token: " + e.getMessage()); // Enhanced error log
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
