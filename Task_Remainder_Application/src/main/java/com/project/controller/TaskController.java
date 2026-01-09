package com.project.controller;

import com.project.entity.Task;
import com.project.service.TaskService;
import com.project.enums.TaskPriority;
import com.project.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Controller 
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "table") String view,
            Model model
    ) {

        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isBlank()) { 
            sortObj = switch (sort) {
                case "dueDate" -> Sort.by("dueDate");
                case "priority" -> Sort.by("priority");
                case "createdAt" -> Sort.by("createdAt");
                case "title" -> Sort.by("title");
                default -> Sort.unsorted();
            };
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Task> taskPage =
                taskService.getPagedTasks(pageable, status, priority, keyword);

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("taskPage", taskPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("view", view);

        List<Integer> pageNumbers = IntStream
                .range(0, taskPage.getTotalPages())
                .boxed()
                .toList();

        model.addAttribute("pageNumbers", pageNumbers);

        return "tasks";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        return "add-task";
    }

    @PostMapping("/add")
    public String saveTask(
            @ModelAttribute Task task,
            Model model,
            RedirectAttributes ra
    ) {

        if (task.getTitle() == null || task.getTitle().isBlank()) {
            model.addAttribute("errorMessage", "Title is required!");
            return "add-task";
        }

        if (task.getDescription() == null || task.getDescription().isBlank()) {
            model.addAttribute("errorMessage", "Description is required!");
            return "add-task";
        }

        if (task.getDueDate() == null) {
            model.addAttribute("errorMessage", "Due date is required!");
            return "add-task";
        }

        taskService.addTask(task);
        ra.addFlashAttribute("successMessage", "Task added successfully!");
        return "redirect:/api/tasks";
    }

    @GetMapping("/update/{id}")
    public String showEditForm( @PathVariable Integer id, Model model,RedirectAttributes ra) {

        return taskService.findById(id).map(task -> {
                    if (task.getStatus() == TaskStatus.COMPLETED) {
                        ra.addFlashAttribute(
                                "errorMessage",
                                "Completed tasks cannot be updated."
                        );
                        return "redirect:/api/tasks";
                    }
                    model.addAttribute("task", task);
                    return "update-task";
                })
                .orElse("redirect:/api/tasks");
    }

    @PostMapping("/update/{id}")
    public String updateTask(
            @PathVariable Integer id,
            @ModelAttribute Task task,
            Model model,
            RedirectAttributes ra
    ) {

        if (task.getTitle() == null || task.getTitle().isBlank()) {
            model.addAttribute("errorMessage", "Title is required!");
            return "update-task";
        }

        if (task.getDueDate() == null) {
            model.addAttribute("errorMessage", "Due date is required!");
            return "update-task";
        }

        task.setId(id);

        taskService.findById(id).ifPresentOrElse(
                existing -> task.setCreatedAt(existing.getCreatedAt()),
                () -> task.setCreatedAt(LocalDate.now())
        );

        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDate.now());
        }

        taskService.updateTask(task);
        ra.addFlashAttribute("successMessage", "Task updated successfully!");
        return "redirect:/api/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id,RedirectAttributes ra) {
        if (taskService.findById(id).isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Task not found!");
            return "redirect:/api/tasks";
        }

        taskService.deleteTask(id);
        ra.addFlashAttribute("successMessage", "Task deleted successfully!");
        return "redirect:/api/tasks";
    }

    @GetMapping("/markAsDone/{id}")
    public String markAsDone(@PathVariable Integer id,RedirectAttributes ra) {

        try {
            taskService.markTask(id);
            ra.addFlashAttribute("successMessage","Task marked as completed!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error Message "  + e.getMessage());
        }
        return "redirect:/api/tasks";
    }
}
