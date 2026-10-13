
package com.workboard.workboard.controller;

import com.workboard.workboard.dto.UserResponse;
import com.workboard.workboard.exception.GlobalExceptionHandler;
import com.workboard.workboard.service.AuthService;

import java.time.LocalDateTime;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerShouldReturn201ForValidRequest() throws Exception {

        when(authService.register(any()))
                .thenReturn(new UserResponse(
                        1L,
                        "Test User",
                        "test@example.com",
                        LocalDateTime.now()
                ));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Test User",
                        "email": "test@example.com",
                        "password": "Password123"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email")
                        .value("test@example.com"));

        verify(authService).register(any());
    }

    @Test
    void registerShouldRejectBlankName() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "",
                        "email": "test@example.com",
                        "password": "Password123"
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void registerShouldRejectInvalidEmail() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Test User",
                        "email": "invalid-email",
                        "password": "Password123"
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void registerShouldRejectShortPassword() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Test User",
                        "email": "test@example.com",
                        "password": "short"
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void registerShouldRejectMissingPassword() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Test User",
                        "email": "test@example.com"
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
    @Test
    void registerShouldReturn409WhenEmailAlreadyExists() throws Exception {

        doThrow(new IllegalArgumentException(
                "An account with this email already exists"))
                .when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "Test User",
                    "email": "test@example.com",
                    "password": "Password123"
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("An account with this email already exists"));

        verify(authService).register(any());
    }
}
