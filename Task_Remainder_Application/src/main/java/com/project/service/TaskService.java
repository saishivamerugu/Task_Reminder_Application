package com.project.service;

import com.project.entity.Task;
import com.project.enums.TaskPriority;
import com.project.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TaskService {

    Page<Task> getPagedTasks(
            Pageable pageable,
            TaskStatus status,
            TaskPriority priority,
            String keyword
    );

    Optional<Task> findById(Integer id);

    void addTask(Task task);

    void updateTask(Task task);

    void markTask(Integer id);

    void deleteTask(Integer id);
}
