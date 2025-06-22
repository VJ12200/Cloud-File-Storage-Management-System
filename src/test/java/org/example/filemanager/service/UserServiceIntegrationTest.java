package org.example.filemanager.service;

import org.example.filemanager.config.TestConfig;
import org.example.filemanager.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testProcessOAuth2User() {
        // Given - Mock OAuth2User
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Test User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/picture.jpg");
        when(oauth2User.getAttribute("sub")).thenReturn("google-user-id-123");

        // When - Process OAuth2 user
        User processedUser = userService.processOAuth2User(oauth2User);

        // Then - Verify user creation
        assertNotNull(processedUser);
        assertEquals("test@example.com", processedUser.getEmail());
        assertEquals("Test User", processedUser.getName());
        assertEquals("https://example.com/picture.jpg", processedUser.getPicture());
        assertEquals("google", processedUser.getProvider());
        assertEquals("google-user-id-123", processedUser.getId());
        assertNotNull(processedUser.getLastLogin());
    }

    @Test
    void testUserExists() {
        // Given - Create a user first
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("exists@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Exists User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/picture.jpg");
        when(oauth2User.getAttribute("sub")).thenReturn("google-user-id-456");

        // Initially user should not exist
        assertFalse(userService.userExists("exists@example.com"));

        // Process OAuth2 user (creates user)
        userService.processOAuth2User(oauth2User);

        // Now user should exist
        assertTrue(userService.userExists("exists@example.com"));
    }

    @Test
    void testGetUserByEmail() {
        // Given - Create a user first
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("retrieve@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Retrieve User");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/picture.jpg");
        when(oauth2User.getAttribute("sub")).thenReturn("google-user-id-789");

        // Process OAuth2 user
        User createdUser = userService.processOAuth2User(oauth2User);

        // When - Retrieve user by email
        User retrievedUser = userService.getUserByEmail("retrieve@example.com");

        // Then - Verify retrieval
        assertNotNull(retrievedUser);
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals(createdUser.getEmail(), retrievedUser.getEmail());
        assertEquals(createdUser.getName(), retrievedUser.getName());
    }

    @Test
    void testGetNonExistentUser() {
        // When - Try to get non-existent user
        User result = userService.getUserByEmail("nonexistent@example.com");

        // Then - Should return null
        assertNull(result);
    }

    @Test
    void testUpdateExistingUser() {
        // Given - Create a user first
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("update@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Original Name");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/original.jpg");
        when(oauth2User.getAttribute("sub")).thenReturn("google-user-id-update");

        // Process OAuth2 user first time
        User originalUser = userService.processOAuth2User(oauth2User);

        // When - Process same user with updated information
        when(oauth2User.getAttribute("name")).thenReturn("Updated Name");
        when(oauth2User.getAttribute("picture")).thenReturn("https://example.com/updated.jpg");

        User updatedUser = userService.processOAuth2User(oauth2User);

        // Then - Verify user was updated, not duplicated
        assertEquals(originalUser.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("https://example.com/updated.jpg", updatedUser.getPicture());
        assertEquals("update@example.com", updatedUser.getEmail());

        // Verify only one user exists with this email
        assertTrue(userService.userExists("update@example.com"));
    }
}
