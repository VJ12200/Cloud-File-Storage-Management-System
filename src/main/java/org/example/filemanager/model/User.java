package org.example.filemanager.model;

import java.time.LocalDateTime;

/**
 * User model to store authenticated user information from Google OAuth2 and local authentication
 */
public class User {
    private String id;
    private String email;
    private String name;
    private String picture;
    private String provider;
    private String password; // For local authentication
    private LocalDateTime lastLogin;

    public User() {}

    public User(String id, String email, String name, String picture, String provider) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.provider = provider;
        this.lastLogin = LocalDateTime.now();
    }

    // Constructor for local authentication
    public User(String email, String name, String password) {
        this.id = java.util.UUID.randomUUID().toString();
        this.email = email;
        this.name = name;
        this.password = password;
        this.provider = "local";
        this.lastLogin = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", provider='" + provider + '\'' +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
