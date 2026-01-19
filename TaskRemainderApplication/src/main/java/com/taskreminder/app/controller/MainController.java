package com.taskreminder.app.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(HttpSession session) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
        }

        return "index";
    }
}