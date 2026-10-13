
package com.workboard.workboard.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TaskSecurityTestConfig {

    @Bean
    PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    static String encodePasswordForTest(String rawPassword) {
        return new BCryptPasswordEncoder().encode(rawPassword);
    }
}
