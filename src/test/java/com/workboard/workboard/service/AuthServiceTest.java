
package com.workboard.workboard.service;

import com.workboard.workboard.dto.RegisterRequest;
import com.workboard.workboard.dto.UserResponse;
import com.workboard.workboard.entity.User;
import com.workboard.workboard.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldSaveUserWithNormalizedEmailAndHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                "  Varsha  ",
                "  VARSHA@example.com  ",
                "Password123"
        );

        when(userRepository.existsByEmailIgnoreCase("varsha@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Varsha", savedUser.getName());
        assertEquals("varsha@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertNotEquals("Password123", savedUser.getPasswordHash());

        assertEquals("Varsha", response.name());
        assertEquals("varsha@example.com", response.email());

        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Varsha",
                "varsha@example.com",
                "Password123"
        );

        when(userRepository.existsByEmailIgnoreCase("varsha@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "An account with this email already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void registerShouldCheckEmailWithoutCaseSensitivity() {
        RegisterRequest request = new RegisterRequest(
                "Varsha",
                "VARSHA@EXAMPLE.COM",
                "Password123"
        );

        when(userRepository.existsByEmailIgnoreCase("varsha@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        verify(userRepository)
                .existsByEmailIgnoreCase("varsha@example.com");
    }
}
