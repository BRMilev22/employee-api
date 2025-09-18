package com.example.employee_api.security;

import com.example.employee_api.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

import java.util.Arrays;

/**
 * Spring Security Configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    // Employee endpoints - different access levels  
                    .requestMatchers("/api/employees/search/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/employees/*/activate").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/employees/*/deactivate").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/employees/profile").hasAnyRole("USER", "SUPER_ADMIN")
                    .requestMatchers("/api/employees/*/profile").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/employees/bulk/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/employees/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    // Department endpoints
                    .requestMatchers("/api/departments/search/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/departments/*/employees").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/departments/*/statistics").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/departments/**").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    // Position endpoints
                    .requestMatchers("/api/positions/search/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/positions/**").hasAnyRole("HR", "ADMIN", "SUPER_ADMIN")
                    // Leave endpoints
                    .requestMatchers("/api/leave-types/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/leave-requests/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/leave-documents/**").hasAnyRole("USER", "HR", "ADMIN", "SUPER_ADMIN")
                    // Admin only endpoints
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/roles/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                    // All other requests require authentication
                    .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}