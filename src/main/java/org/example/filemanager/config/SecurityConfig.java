package org.example.filemanager.config;

import org.example.filemanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.annotation.PostConstruct;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Spring Security configuration for Google OAuth2 authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @PostConstruct
    public void logOAuth2Configuration() {
        logger.info("=== OAuth2 Configuration Debug ===");
        logger.info("Google Client ID: {}", googleClientId);
        logger.info("Google Client Secret: {} (length: {})",
                   googleClientSecret != null ? googleClientSecret.substring(0, Math.min(10, googleClientSecret.length())) + "..." : "null",
                   googleClientSecret != null ? googleClientSecret.length() : 0);
        logger.info("Client ID is null: {}", googleClientId == null);
        logger.info("Client Secret is null: {}", googleClientSecret == null);
        logger.info("Client ID is empty: {}", googleClientId != null && googleClientId.isEmpty());
        logger.info("Client Secret is empty: {}", googleClientSecret != null && googleClientSecret.isEmpty());
        logger.info("=== End OAuth2 Configuration Debug ===");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/login", "/error", "/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oauth2AuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Authentication authentication) throws IOException, ServletException {

                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

                // Process the OAuth2 user and store in our user service
                var user = userService.processOAuth2User(oauth2User);

                // Store user in session for easy access
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                logger.info("User {} successfully authenticated via Google OAuth2", user.getEmail());

                // Redirect to the main application
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {

                // Clear any user data from session
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                logger.info("User successfully logged out");

                // Redirect to login page with logout parameter
                response.sendRedirect("/login?logout=true");
            }
        };
    }
}
