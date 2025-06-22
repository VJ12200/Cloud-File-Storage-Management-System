package org.example.filemanager.service;

import org.example.filemanager.config.TestConfig;
import org.example.filemanager.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterAndAuthenticateLocalUser() {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String password = "password123";

        // When - Register user
        User registeredUser = userService.registerLocalUser(email, name, password);

        // Then - Verify registration
        assertNotNull(registeredUser);
        assertEquals(email, registeredUser.getEmail());
        assertEquals(name, registeredUser.getName());
        assertEquals("local", registeredUser.getProvider());
        assertNotNull(registeredUser.getId());
        assertNotNull(registeredUser.getLastLogin());

        // When - Authenticate user
        User authenticatedUser = userService.authenticateLocalUser(email, password);

        // Then - Verify authentication
        assertNotNull(authenticatedUser);
        assertEquals(email, authenticatedUser.getEmail());
        assertEquals(name, authenticatedUser.getName());
    }

    @Test
    void testUserExists() {
        // Given
        String email = "exists@example.com";
        String name = "Exists User";
        String password = "password123";

        // Initially user should not exist
        assertFalse(userService.userExists(email));

        // Register user
        userService.registerLocalUser(email, name, password);

        // Now user should exist
        assertTrue(userService.userExists(email));
    }

    @Test
    void testAuthenticateNonExistentUser() {
        // When
        User result = userService.authenticateLocalUser("nonexistent@example.com", "password");

        // Then
        assertNull(result);
    }

    @Test
    void testRegisterDuplicateUser() {
        // Given
        String email = "duplicate@example.com";
        String name = "Duplicate User";
        String password = "password123";

        // Register user first time
        userService.registerLocalUser(email, name, password);

        // When & Then - Try to register again
        RuntimeException exception = assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userService.registerLocalUser(email, name, password);
            }
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }
}
