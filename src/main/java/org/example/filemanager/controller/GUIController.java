package org.example.filemanager.controller;

import org.example.filemanager.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class GUIController {
    private static final Logger logger = LoggerFactory.getLogger(GUIController.class);

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User principal,
                       HttpSession session,
                       Model model) {
        if (principal != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                model.addAttribute("user", user);
                logger.info("User {} accessing file manager", user.getEmail());
            }
        }
        return "file-manager";
    }

    @GetMapping("/file-manager")
    public String fileManager(@AuthenticationPrincipal OAuth2User principal,
                             HttpSession session,
                             Model model) {
        if (principal != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                model.addAttribute("user", user);
                logger.info("User {} accessing file manager", user.getEmail());
            }
        }
        return "file-manager";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
