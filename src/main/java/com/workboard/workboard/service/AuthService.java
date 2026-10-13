
package com.workboard.workboard.service;

import com.workboard.workboard.dto.RegisterRequest;
import com.workboard.workboard.dto.UserResponse;
import com.workboard.workboard.entity.User;
import com.workboard.workboard.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        User user = new User(
                request.name().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}
