package com.taskreminder.app.repository;

import com.taskreminder.app.entity.Task;
import com.taskreminder.app.enums.TaskPriority;
import com.taskreminder.app.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // ---------------- PAGINATED FILTERED TASKS ----------------

    Page<Task> findByUserId(Integer userId, Pageable pageable);

    Page<Task> findByUserIdAndStatus(
            Integer userId,
            TaskStatus status,
            Pageable pageable
    );

    Page<Task> findByUserIdAndPriority(
            Integer userId,
            TaskPriority priority,
            Pageable pageable
    );

    Page<Task> findByUserIdAndTitleContainingIgnoreCase(
            Integer userId,
            String keyword,
            Pageable pageable
    );

    // ---------------- DASHBOARD COUNTS ----------------

    List<Task> findByUserIdAndDueDateBeforeAndStatusNot(
            Integer userId,
            LocalDate date,
            TaskStatus status
    );

    List<Task> findByUserIdAndDueDate(
            Integer userId,
            LocalDate date
    );

    List<Task> findByUserIdAndDueDateAfterAndStatusNot(
            Integer userId,
            LocalDate date,
            TaskStatus status
    );

    List<Task> findByUserIdAndStatus(
            Integer userId,
            TaskStatus status
    );

    List<Task> findByUserId(Integer userId);

    // ---------------- REMINDERS ----------------

    List<Task> findByUserIdAndReminderTimeBetweenAndReminderSentFalse(
            Integer userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // For scheduler (global reminders, not user-specific)
    List<Task> findByReminderTimeBetweenAndReminderSentFalse(
            LocalDateTime start,
            LocalDateTime end
    );
}