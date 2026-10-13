package com.workboard.workboard.controller;

import com.workboard.workboard.entity.Task;
import com.workboard.workboard.service.TaskService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "IN_PROGRESS", "COMPLETED");
    private static final Set<String> ALLOWED_PRIORITIES =
        Set.of("HIGH", "MEDIUM", "LOW");

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    
@PostMapping
public ResponseEntity<?> createTask(
        @Valid @RequestBody Task task,
        Principal principal) {

    if (task.getPriority() == null ||
            !ALLOWED_PRIORITIES.contains(task.getPriority())) {
        return ResponseEntity.badRequest().body(
                Map.of("error",
                        "Priority must be HIGH, MEDIUM, or LOW"));
    }

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(taskService.createTask(task, principal.getName()));
}


    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(Principal principal) {
        return ResponseEntity.ok(
                taskService.getAllTasks(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(
            @PathVariable Long id,
            Principal principal) {

        return taskService.getTaskById(id, principal.getName())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Task not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long id,
            Principal principal) {

        boolean deleted = taskService.deleteTask(id, principal.getName());

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        }

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Principal principal) {

        String status = request.get("status");

        if (status == null || !ALLOWED_STATUSES.contains(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",
                    "Status must be PENDING, IN_PROGRESS, or COMPLETED"));
        }

        return taskService.updateTaskStatus(
                        id, status, principal.getName())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Task not found")));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTasksByStatus(
            @PathVariable String status,
            Principal principal) {

        if (!ALLOWED_STATUSES.contains(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",
                    "Status must be PENDING, IN_PROGRESS, or COMPLETED"));
        }

        return ResponseEntity.ok(
                taskService.getTasksByStatus(status, principal.getName()));
    }

    @GetMapping("/newest")
    public ResponseEntity<List<Task>> getTasksNewestFirst(
            Principal principal) {

        return ResponseEntity.ok(
                taskService.getTasksNewestFirst(principal.getName()));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Task>> getTasksPaginated(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative")
            int page,

            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            int size,

            Principal principal) {

        return ResponseEntity.ok(
                taskService.getTasksPaginated(
                        page, size, principal.getName()));
    }

    
@PutMapping("/{id}")
public ResponseEntity<?> updateTask(
        @PathVariable Long id,
        @Valid @RequestBody Task updatedTask,
        Principal principal) {

    // Validate priority only when the request includes one.
    if (updatedTask.getPriority() != null &&
            !ALLOWED_PRIORITIES.contains(updatedTask.getPriority())) {
        return ResponseEntity.badRequest().body(
                Map.of("error",
                        "Priority must be HIGH, MEDIUM, or LOW"));
    }

    return taskService.updateTask(
                    id, updatedTask, principal.getName())
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found")));
}

}
