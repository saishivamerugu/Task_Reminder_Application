package com.taskreminder.app.controller;

import com.taskreminder.app.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("overdueTasks", taskService.getOverdueTasks(userId));
        model.addAttribute("todayTasks", taskService.getTodayTasks(userId));
        model.addAttribute("upcomingTasks", taskService.getUpcomingTasks(userId));
        model.addAttribute("completedTasks", taskService.getCompletedTasks(userId));
        model.addAttribute("pendingTasks", taskService.getPendingTasks(userId));
        model.addAttribute("allTasks", taskService.getAllTasks(userId));

        return "dashboard";
    }
}
