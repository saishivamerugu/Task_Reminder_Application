package com.taskreminder.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("TimeIt - Email Verification OTP");
        message.setText(
                "Your OTP for email verification is: " + otp +
                "\n\nThis OTP is valid for 5 minutes."
        );

        mailSender.send(message);
    }

    public void sendReminderEmail(String toEmail, String taskTitle) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("⏰ Task Reminder - TimeIt");
        message.setText(
                "Reminder for your task: \"" + taskTitle + "\"\n\n" +
                "Don't forget to complete it on time!"
        );

        mailSender.send(message);
    }
}