package com.project.service.impl;

import com.project.entity.Task;
import com.project.enums.TaskPriority;
import com.project.enums.TaskStatus;
import com.project.repository.TaskRepository;
import com.project.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Page<Task> getPagedTasks(
            Pageable pageable,
            TaskStatus status,
            TaskPriority priority,
            String keyword
    ) {
        return taskRepository.findTasksWithFilters(
                status,
                priority,
                (keyword == null || keyword.isBlank()) ? null : keyword.trim(),
                pageable
        );
    }

    @Override
    public Optional<Task> findById(Integer id) {
        return taskRepository.findById(id);
    }

    @Override
    public void addTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public void updateTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public void markTask(Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException("Task not found")
                );

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task already completed");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDate.now());

        taskRepository.save(task);
    }

    @Override
    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }
}
