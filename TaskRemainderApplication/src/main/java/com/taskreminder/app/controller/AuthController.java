package com.taskreminder.app.controller;

import com.taskreminder.app.dto.LoginRequest;
import com.taskreminder.app.dto.RegisterRequest;
import com.taskreminder.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // ---------------- LOGIN ----------------

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @ModelAttribute LoginRequest request,
            HttpSession session,
            Model model
    ) {

        Integer userId = userService.login(request);

        if (userId == null) {
            model.addAttribute("errorMessage", "Invalid email or password");
            return "login";
        }

        session.setAttribute("userId", userId);
        return "redirect:/dashboard";
    }

    // ---------------- REGISTER ----------------

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute RegisterRequest request,
            Model model
    ) {

        String result = userService.register(request);

        if (!"SUCCESS".equals(result)) {
            model.addAttribute("errorMessage", result);
            return "register";
        }

        model.addAttribute(
                "successMessage",
                "Account created successfully. Please login."
        );
        return "login";
    }

    // ---------------- LOGOUT ----------------

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}