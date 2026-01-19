package com.taskreminder.app.service;

import com.taskreminder.app.dto.LoginRequest;
import com.taskreminder.app.dto.RegisterRequest;
import com.taskreminder.app.dto.UpdateProfileRequest;
import com.taskreminder.app.entity.User;
import com.taskreminder.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // ---------------- LOGIN ----------------

    public Integer login(LoginRequest request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return null;
        }

        return user.getId();
    }

    // ---------------- REGISTER ----------------

    @Transactional
    public String register(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Passwords do not match";
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already registered";
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);

        userRepository.save(user);
        return "SUCCESS";
    }

    // ---------------- PROFILE ----------------

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Transactional
    public String updateProfile(
            Integer userId,
            UpdateProfileRequest request,
            MultipartFile profileImage
    ) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found";
        }

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(request.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                return "Email already in use by another account";
            }
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Validate file type
                String contentType = profileImage.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return "Invalid file type. Please upload an image file.";
                }

                // Validate file size (max 2MB)
                if (profileImage.getSize() > 2 * 1024 * 1024) {
                    return "File size must be less than 2 MB";
                }

                // Generate safe filename
                String originalFilename = profileImage.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = UUID.randomUUID() + extension;

                // Create directory if not exists
                Path uploadPath = Paths.get("uploads/profiles");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Save file
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(profileImage.getInputStream(), filePath);

                // Delete old profile image if exists
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    try {
                        String oldImagePath = user.getProfileImage().replace("/uploads/profiles/", "");
                        Path oldFilePath = uploadPath.resolve(oldImagePath);
                        Files.deleteIfExists(oldFilePath);
                    } catch (IOException e) {
                        // Log but don't fail the update
                        System.err.println("Failed to delete old profile image: " + e.getMessage());
                    }
                }

                user.setProfileImage("/uploads/profiles/" + fileName);

            } catch (IOException e) {
                return "Failed to upload image: " + e.getMessage();
            }
        }

        userRepository.save(user);
        return "SUCCESS";
    }

    // ---------------- EMAIL VERIFICATION ----------------

    @Transactional
    public void sendVerificationOtp(Integer userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            // You might want to throw an exception here or handle it appropriately
        }
    }

    @Transactional
    public boolean verifyEmailOtp(Integer userId, String otp) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            return false;
        }

        if (!user.getOtp().equals(otp)) {
            return false;
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return true;
    }
}