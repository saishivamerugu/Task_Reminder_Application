package com.taskreminder.app.service;

import com.taskreminder.app.entity.Task;
import com.taskreminder.app.entity.User;
import com.taskreminder.app.enums.TaskPriority;
import com.taskreminder.app.enums.TaskStatus;
import com.taskreminder.app.repository.TaskRepository;
import com.taskreminder.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // ---------------- VIEW TASKS ----------------

    public Page<Task> getTasks(
            Integer userId,
            int page,
            int size,
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            String sort,
            String view
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sort == null || sort.isBlank() ? "createdAt" : sort).descending()
        );

        if (keyword != null && !keyword.isBlank()) {
            return taskRepository.findByUserIdAndTitleContainingIgnoreCase(
                    userId, keyword, pageable
            );
        }

        if (status != null) {
            return taskRepository.findByUserIdAndStatus(
                    userId, status, pageable
            );
        }

        if (priority != null) {
            return taskRepository.findByUserIdAndPriority(
                    userId, priority, pageable
            );
        }

        return taskRepository.findByUserId(userId, pageable);
    }

    // ---------------- ADD TASK ----------------

    @Transactional
    public void addTask(Task task, Integer userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        task.setUser(user);
        taskRepository.save(task);
    }

    // ---------------- UPDATE TASK ----------------

    public Task getTaskById(Long id, Integer userId) {

        return taskRepository.findById(id)
                .filter(task -> task.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Transactional
    public void updateTask(Long id, Task updatedTask, Integer userId) {

        Task existing = getTaskById(id, userId);

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setPriority(updatedTask.getPriority());
        existing.setStatus(updatedTask.getStatus());
        existing.setReminderSent(updatedTask.isReminderSent());
        existing.setReminderTime(updatedTask.getReminderTime());

        if (updatedTask.getStatus() == TaskStatus.COMPLETED) {
            existing.setCompletedAt(LocalDateTime.now());
        }

        taskRepository.save(existing);
    }

    // ---------------- DELETE ----------------

    @Transactional
    public void deleteTask(Long id, Integer userId) {
        Task task = getTaskById(id, userId);
        taskRepository.delete(task);
    }

    // ---------------- MARK DONE ----------------

    @Transactional
    public void markAsDone(Long id, Integer userId) {

        Task task = getTaskById(id, userId);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        taskRepository.save(task);
    }

    // ---------------- DASHBOARD COUNTS ----------------

    public List<Task> getOverdueTasks(Integer userId) {
        return taskRepository.findByUserIdAndDueDateBeforeAndStatusNot(
                userId, LocalDate.now(), TaskStatus.COMPLETED
        );
    }

    public List<Task> getTodayTasks(Integer userId) {
        return taskRepository.findByUserIdAndDueDate(userId, LocalDate.now());
    }

    public List<Task> getUpcomingTasks(Integer userId) {
        return taskRepository.findByUserIdAndDueDateAfterAndStatusNot(
                userId, LocalDate.now(), TaskStatus.COMPLETED
        );
    }

    public List<Task> getCompletedTasks(Integer userId) {
        return taskRepository.findByUserIdAndStatus(
                userId, TaskStatus.COMPLETED
        );
    }

    public List<Task> getPendingTasks(Integer userId) {
        return taskRepository.findByUserIdAndStatus(
                userId, TaskStatus.PENDING
        );
    }

    public List<Task> getAllTasks(Integer userId) {
        return taskRepository.findByUserId(userId);
    }

    // ---------------- REMINDERS ----------------

    public List<Task> getUpcomingReminders(Integer userId) {

        LocalDateTime now = LocalDateTime.now();

        return taskRepository.findByUserIdAndReminderTimeBetweenAndReminderSentFalse(
                userId, now, now.plusHours(24)
        );
    }

    // ---------------- SCHEDULER SUPPORT ----------------

    public List<Task> getUpcomingRemindersForScheduler() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusMinutes(1);
        LocalDateTime end = now.plusMinutes(1);

        return taskRepository.findByReminderTimeBetweenAndReminderSentFalse(start, end);
    }

    @Transactional
    public void save(Task task) {
        taskRepository.save(task);
    }
}