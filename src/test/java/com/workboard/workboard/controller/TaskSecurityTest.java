
package com.workboard.workboard.controller;

import com.workboard.workboard.config.SecurityConfig;
import com.workboard.workboard.service.CustomUserDetailsService;
import com.workboard.workboard.service.TaskService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, TaskSecurityTestConfig.class})
class TaskSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getTasksWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTaskWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Test task",
                        "description": "Test description"
                    }
                    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTaskByIdWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTaskWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Updated task",
                        "description": "Updated description"
                    }
                    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTaskWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTaskStatusWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "COMPLETED"}
                    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasksByStatusWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/tasks/status/PENDING"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getNewestTasksWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/tasks/newest"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaginatedTasksWithoutAuthenticationShouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/tasks/page"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedRequestsShouldNotReachTaskService()
            throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Test task",
                        "description": "Test description"
                    }
                    """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    @Test
    void getTasksWithInvalidCredentialsShouldReturn401()
            throws Exception {

        when(customUserDetailsService.loadUserByUsername(
                "test@example.com"))
                .thenThrow(
                        new org.springframework.security.core.userdetails
                                .UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/tasks")
                .with(httpBasic(
                        "test@example.com", "WrongPassword123")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    @Test
    void getTasksWithValidCredentialsShouldReachTaskService()
            throws Exception {

        String rawPassword = "CorrectPassword123";

        UserDetails user = User.withUsername("test@example.com")
                .password(TaskSecurityTestConfig
                        .encodePasswordForTest(rawPassword))
                .roles("USER")
                .build();

        when(customUserDetailsService.loadUserByUsername(
                "test@example.com"))
                .thenReturn(user);

        mockMvc.perform(get("/api/tasks")
                .with(httpBasic("test@example.com", rawPassword)))
                .andExpect(status().isOk());

        verify(taskService).getAllTasks("test@example.com");
    }
}
