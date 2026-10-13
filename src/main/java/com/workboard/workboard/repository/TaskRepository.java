
package com.workboard.workboard.repository;

import com.workboard.workboard.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(String status);

    List<Task> findAll(Sort sort);

    // Find tasks belonging to a specific user.
    List<Task> findByOwnerId(Long ownerId);

    // Find one task only if it belongs to the specified user.
    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);

    // Find a user's tasks filtered by status.
    List<Task> findByOwnerIdAndStatus(Long ownerId, String status);

    // Retrieve a user's tasks with pagination and sorting.
    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

    List<Task> findByOwnerId(Long ownerId, Sort sort);
}
