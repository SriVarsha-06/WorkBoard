package com.workboard.workboard.controller;

import java.util.Optional;

import com.workboard.workboard.entity.Task;
import com.workboard.workboard.exception.GlobalExceptionHandler;
import com.workboard.workboard.service.TaskService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    private static final String TEST_EMAIL = "workboard-test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getExistingTaskShouldReturn200() throws Exception {
        Task task = new Task("Study Java", "Revise collections");

        when(taskService.getTaskById(1L, TEST_EMAIL))
                .thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/tasks/1")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Study Java"));
    }

    @Test
    void getMissingTaskShouldReturn404() throws Exception {
        when(taskService.getTaskById(999L, TEST_EMAIL))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/999")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found"));
    }

    @Test
    void createTaskWithBlankTitleShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "title": " ",
                    "description": "Test task"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .principal(() -> TEST_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMissingTaskShouldReturn404() throws Exception {
        when(taskService.updateTask(
                eq(999L),
                any(Task.class),
                eq(TEST_EMAIL)))
                .thenReturn(Optional.empty());

        String requestBody = """
                {
                    "title": "Updated task",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(put("/api/tasks/999")
                        .principal(() -> TEST_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found"));
    }

    @Test
    void updateTaskWithInvalidStatusShouldReturn400() throws Exception {
        String requestBody = """
                {
                    "status": "INVALID"
                }
                """;

        mockMvc.perform(patch("/api/tasks/1/status")
                        .principal(() -> TEST_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getTasksWithNegativePageShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/tasks/page?page=-1&size=5")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMissingTaskShouldReturn404() throws Exception {
        when(taskService.deleteTask(999L, TEST_EMAIL))
                .thenReturn(false);

        mockMvc.perform(delete("/api/tasks/999")
                        .principal(() -> TEST_EMAIL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found"));
    }
}