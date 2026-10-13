
package com.workboard.workboard.service;

import com.workboard.workboard.entity.Task;
import com.workboard.workboard.entity.User;
import com.workboard.workboard.repository.TaskRepository;
import com.workboard.workboard.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String TEST_EMAIL =
            "workboard-test@example.com";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();

        testUser.setName("Test User");
        testUser.setEmail(TEST_EMAIL);
        testUser.setPasswordHash("test-password-hash");

        // Assign a predictable ID for unit testing.
        ReflectionTestUtils.setField(testUser, "id", 1L);

        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void createTaskShouldSaveTask() {
        Task task = new Task(
                "Finish project",
                "Complete backend"
        );

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task, TEST_EMAIL);

        assertEquals("Finish project", result.getTitle());
        verify(userRepository).findByEmailIgnoreCase(TEST_EMAIL);
        verify(taskRepository).save(task);
    }

    @Test
    void getTaskByIdShouldReturnTaskWhenFound() {
        Task task = new Task(
                "Study Java",
                "Revise collections"
        );

        when(taskRepository.findByIdAndOwnerId(
                1L, testUser.getId()
        )).thenReturn(Optional.of(task));

        Optional<Task> result =
                taskService.getTaskById(1L, TEST_EMAIL);

        assertTrue(result.isPresent());
        assertEquals("Study Java", result.get().getTitle());
    }

    @Test
    void updateTaskShouldUpdateTitleAndDescription() {
        Task existingTask = new Task(
                "Old title",
                "Old description"
        );

        Task updatedTask = new Task(
                "New title",
                "New description"
        );
        updatedTask.setPriority("HIGH");

        when(taskRepository.findByIdAndOwnerId(
                1L, testUser.getId()
        )).thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask))
                .thenReturn(existingTask);

        Optional<Task> result = taskService.updateTask(
                1L, updatedTask, TEST_EMAIL
        );

        assertTrue(result.isPresent());
        assertEquals("New title", result.get().getTitle());
        assertEquals(
                "New description",
                result.get().getDescription()
        );

        verify(taskRepository).save(existingTask);
        assertEquals("HIGH", result.get().getPriority());
    }

    @Test
    void updateTaskShouldReturnEmptyWhenTaskDoesNotExist() {
        Task updatedTask = new Task(
                "New title",
                "New description"
        );

        when(taskRepository.findByIdAndOwnerId(
                999L, testUser.getId()
        )).thenReturn(Optional.empty());

        Optional<Task> result = taskService.updateTask(
                999L, updatedTask, TEST_EMAIL
        );

        assertTrue(result.isEmpty());

        verify(taskRepository, never())
                .save(any(Task.class));
    }

    @Test
    void getTaskByIdShouldReturnEmptyWhenTaskDoesNotExist() {
        when(taskRepository.findByIdAndOwnerId(
                999L, testUser.getId()
        )).thenReturn(Optional.empty());

        Optional<Task> result =
                taskService.getTaskById(999L, TEST_EMAIL);

        assertTrue(result.isEmpty());
    }
    
        @Test
        void getTaskByIdShouldNotReturnAnotherUsersTask() {
        when(taskRepository.findByIdAndOwnerId(10L, 1L))
                .thenReturn(Optional.empty());

        Optional<Task> result =
                taskService.getTaskById(10L, TEST_EMAIL);

        assertTrue(result.isEmpty());

        verify(taskRepository)
                .findByIdAndOwnerId(10L, 1L);
        }

        @Test
        void updateTaskShouldNotUpdateAnotherUsersTask() {
        Task updatedTask = new Task(
        "Changed title",
        "Changed description"
        );

        when(taskRepository.findByIdAndOwnerId(10L, 1L))
                .thenReturn(Optional.empty());

        Optional<Task> result =
                taskService.updateTask(10L, updatedTask, TEST_EMAIL);

        assertTrue(result.isEmpty());

        verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void deleteTaskShouldNotDeleteAnotherUsersTask() {
        when(taskRepository.findByIdAndOwnerId(10L, 1L))
                .thenReturn(Optional.empty());

        boolean deleted =
                taskService.deleteTask(10L, TEST_EMAIL);

        assertFalse(deleted);

        verify(taskRepository, never()).delete(any(Task.class));
        }

}