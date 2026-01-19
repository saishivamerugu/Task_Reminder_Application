package com.taskreminder.app.controller;

import com.taskreminder.app.entity.Task;
import com.taskreminder.app.enums.TaskPriority;
import com.taskreminder.app.enums.TaskStatus;
import com.taskreminder.app.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ---------------- VIEW TASKS ----------------

    @GetMapping
    public String viewTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "table") String view,
            HttpSession session,
            Model model
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        Page<Task> taskPage = taskService.getTasks(
                userId, page, size, keyword, status, priority, sort, view
        );

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("view", view);

        model.addAttribute("overdueTasks", taskService.getOverdueTasks(userId));
        model.addAttribute("todayTasks", taskService.getTodayTasks(userId));
        model.addAttribute("upcomingTasks", taskService.getUpcomingTasks(userId));
        model.addAttribute("completedTasks", taskService.getCompletedTasks(userId));
        model.addAttribute("pendingTasks", taskService.getPendingTasks(userId));
        model.addAttribute("allTasks", taskService.getAllTasks(userId));
        model.addAttribute("upcomingReminders", taskService.getUpcomingReminders(userId));

        return "tasks";
    }

    // ---------------- ADD TASK ----------------

    @GetMapping("/add")
    public String addTaskPage(HttpSession session, Model model) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("task", new Task());
        return "add-task";
    }

    @PostMapping("/add")
    public String addTask(
            @ModelAttribute Task task,
            HttpSession session,
            Model model
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        taskService.addTask(task, userId);
        model.addAttribute("successMessage", "Task added successfully");

        return "redirect:/api/tasks";
    }

    // ---------------- UPDATE TASK ----------------

    @GetMapping("/update/{id}")
    public String updateTaskPage(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        Task task = taskService.getTaskById(id, userId);
        model.addAttribute("task", task);

        return "update-task";
    }

    @PostMapping("/update/{id}")
    public String updateTask(
            @PathVariable Long id,
            @ModelAttribute Task task,
            HttpSession session
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        taskService.updateTask(id, task, userId);
        return "redirect:/api/tasks";
    }

    // ---------------- DELETE ----------------

    @GetMapping("/delete/{id}")
    public String deleteTask(
            @PathVariable Long id,
            HttpSession session
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        taskService.deleteTask(id, userId);
        return "redirect:/api/tasks";
    }

    // ---------------- MARK AS DONE ----------------

    @GetMapping("/markAsDone/{id}")
    public String markAsDone(
            @PathVariable Long id,
            HttpSession session
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        taskService.markAsDone(id, userId);
        return "redirect:/api/tasks";
    }
}