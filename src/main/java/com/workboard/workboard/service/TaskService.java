
package com.workboard.workboard.service;

import com.workboard.workboard.entity.Task;
import com.workboard.workboard.entity.User;
import com.workboard.workboard.repository.TaskRepository;
import com.workboard.workboard.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public Task createTask(Task task, String email) {
        User owner = getUser(email);
        task.setOwner(owner);
        task.setId(null);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks(String email) {
        return taskRepository.findByOwnerId(getUser(email).getId());
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id, String email) {
        return taskRepository.findByIdAndOwnerId(
                id, getUser(email).getId());
    }

    @Transactional
    public boolean deleteTask(Long id, String email) {
        Optional<Task> task = getTaskById(id, email);
        if (task.isEmpty()) {
            return false;
        }

        taskRepository.delete(task.get());
        return true;
    }

    @Transactional
    public Optional<Task> updateTaskStatus(
            Long id, String status, String email) {
        return getTaskById(id, email).map(task -> {
            task.setStatus(status);
            return taskRepository.save(task);
        });
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(String status, String email) {
        return taskRepository.findByOwnerIdAndStatus(
                getUser(email).getId(), status);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksNewestFirst(String email) {
        return taskRepository.findByOwnerId(
                getUser(email).getId(),
                Sort.by(Sort.Order.desc("createdAt").nullsLast()));
    }

    @Transactional(readOnly = true)
    public Page<Task> getTasksPaginated(
            int page, int size, String email) {
        return taskRepository.findByOwnerId(
                getUser(email).getId(),
                PageRequest.of(page, size,
                        Sort.by(Sort.Order.desc("createdAt").nullsLast())));
    }

    

@Transactional
public Optional<Task> updateTask(
        Long id, Task updatedTask, String email) {

    return getTaskById(id, email).map(existingTask -> {
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());

        if (updatedTask.getPriority() != null) {
            existingTask.setPriority(updatedTask.getPriority());
        }

        // Save the due date, including null when it is cleared.
        existingTask.setDueDate(updatedTask.getDueDate());

        return taskRepository.save(existingTask);
    });
}


}
