package com.taskreminder.app.controller;

import com.taskreminder.app.dto.UpdateProfileRequest;
import com.taskreminder.app.entity.User;
import com.taskreminder.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    // ---------------- VIEW PROFILE ----------------

    @GetMapping
    public String profile(HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.getUserById(userId).orElse(null);
        model.addAttribute("profile", user);

        return "profile";
    }

    // ---------------- EDIT PROFILE ----------------

    @GetMapping("/edit")
    public String editProfile(HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.getUserById(userId).orElse(null);
        model.addAttribute("profile", user);

        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            @ModelAttribute UpdateProfileRequest request,
            @RequestParam(required = false) MultipartFile profileImage,
            HttpSession session,
            Model model
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        String result = userService.updateProfile(userId, request, profileImage);

        if (!"SUCCESS".equals(result)) {
            model.addAttribute("errorMessage", result);
            return "profile-edit";
        }

        model.addAttribute("successMessage", "Profile updated successfully");
        return "redirect:/profile";
    }

    // ---------------- EMAIL VERIFICATION ----------------

    @PostMapping("/send-otp")
    public String sendOtp(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        userService.sendVerificationOtp(userId);
        return "redirect:/profile/edit";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        boolean verified = userService.verifyEmailOtp(userId, otp);

        if (!verified) {
            model.addAttribute("errorMessage", "Invalid or expired OTP");
            return "profile-edit";
        }

        model.addAttribute("successMessage", "Email verified successfully");
        return "redirect:/profile";
    }
}