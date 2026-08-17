package com.sp.web.security; // <-- Make sure this matches your package!

import com.sp.web.domain.User;
import com.sp.web.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
@Configuration
@EnableConfigurationProperties(SecurityConstant.class)
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityConstant securityConstants;
    private final AuthenticationConfiguration configuration;
    private final UserRepository userRepository;
    // constructor for the configs
    @Autowired
    public SecurityConfig(final SecurityConstant securityConstants,
                          AuthenticationConfiguration configuration,
                          UserRepository userRepository) {
        this.securityConstants = securityConstants;
        this.configuration = configuration;
        this.userRepository = userRepository;
    }
    // filter chain methode
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/api/users/updateAccountLockStatus/**").hasRole("admin")
                    .requestMatchers("/api/users/showUsers/").authenticated()
                    .requestMatchers("/api/users/").hasRole("admin")
                    .requestMatchers("/api/users/deleteUser/**").hasRole("admin")
                    .requestMatchers("/api/settings/saveSettings/**").hasRole("admin")
                    .requestMatchers("/api/users/updateUser/**").permitAll()
                    .requestMatchers("/api/users/forgot-password").permitAll()
                    .requestMatchers("/api/users/reset-password").permitAll()
                    .requestMatchers("/api/users/register").permitAll()
                    .anyRequest().permitAll())
            .addFilter(new JwtAuthenticationFilter(authenticationManager(), securityConstants))
            .addFilter(new JwtAuthorizationFilter(authenticationManager(), userDetailsService, securityConstants, userRepository))
            .build();
    }
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    // configuration source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "PATCH", "DELETE"));
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}