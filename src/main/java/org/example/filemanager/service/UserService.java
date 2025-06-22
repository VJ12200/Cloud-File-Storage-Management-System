package org.example.filemanager.service;

import org.example.filemanager.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage user authentication and session data
 * In a production environment, this would typically use a database
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // In-memory storage for demo purposes
    // In production, use a database like PostgreSQL, MySQL, etc.
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    /**
     * Process OAuth2 user and create/update user record
     */
    public User processOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        String id = oauth2User.getAttribute("sub"); // Google's unique user ID
        
        logger.info("Processing OAuth2 user: email={}, name={}", email, name);
        
        User user = users.get(email);
        if (user == null) {
            // Create new user
            user = new User(id, email, name, picture, "google");
            users.put(email, user);
            logger.info("Created new user: {}", user);
        } else {
            // Update existing user
            user.setName(name);
            user.setPicture(picture);
            user.setLastLogin(LocalDateTime.now());
            logger.info("Updated existing user: {}", user);
        }
        
        return user;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return users.get(email);
    }

    /**
     * Get all users (for admin purposes)
     */
    public java.util.Collection<User> getAllUsers() {
        return users.values();
    }

    /**
     * Remove user (for logout/cleanup)
     */
    public void removeUser(String email) {
        users.remove(email);
        logger.info("Removed user with email: {}", email);
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String email) {
        return users.containsKey(email);
    }
}
