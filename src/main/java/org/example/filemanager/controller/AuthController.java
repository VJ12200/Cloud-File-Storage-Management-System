package org.example.filemanager.controller;

import org.example.filemanager.model.User;
import org.example.filemanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;

/**
 * Controller for handling form-based authentication (login and registration)
 */
@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    /**
     * Handle local user login
     */
    @PostMapping("/login/local")
    public String loginLocal(@RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticateLocalUser(email, password);

            if (user != null) {
                // Create authentication token
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Store user in session
                session.setAttribute("user", user);

                logger.info("User {} successfully authenticated via local login", user.getEmail());
                return "redirect:/";
            } else {
                redirectAttributes.addAttribute("error", "invalid");
                return "redirect:/login";
            }
        } catch (Exception e) {
            logger.error("Error during local authentication", e);
            redirectAttributes.addAttribute("error", "system");
            return "redirect:/login";
        }
    }

    /**
     * Handle user registration
     */
    @PostMapping("/register")
    public String register(@RequestParam String email,
                         @RequestParam String name,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addAttribute("error", "email_required");
                return "redirect:/login";
            }

            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addAttribute("error", "name_required");
                return "redirect:/login";
            }

            if (password == null || password.length() < 6) {
                redirectAttributes.addAttribute("error", "password_weak");
                return "redirect:/login";
            }

            if (!password.equals(confirmPassword)) {
                redirectAttributes.addAttribute("error", "password_mismatch");
                return "redirect:/login";
            }

            // Check if user already exists
            if (userService.userExists(email)) {
                redirectAttributes.addAttribute("error", "user_exists");
                return "redirect:/login";
            }

            // Register user
            User user = userService.registerLocalUser(email.trim(), name.trim(), password);

            // Auto-login after registration
            Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("user", user);

            logger.info("User {} successfully registered and logged in", user.getEmail());
            redirectAttributes.addAttribute("registered", "true");
            return "redirect:/";

        } catch (RuntimeException e) {
            logger.error("Error during user registration: {}", e.getMessage());
            redirectAttributes.addAttribute("error", "user_exists");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            redirectAttributes.addAttribute("error", "system");
            return "redirect:/login";
        }
    }
}
