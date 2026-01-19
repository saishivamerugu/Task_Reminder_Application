package com.taskreminder.app.scheduler;

import com.taskreminder.app.entity.Task;
import com.taskreminder.app.service.EmailService;
import com.taskreminder.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private TaskService taskService;

    @Autowired
    private EmailService emailService;

    /**
     * Runs every 1 minute to check for tasks that need reminders sent.
     * Looks for tasks with reminderTime within the next minute that haven't been sent yet.
     */
    @Scheduled(fixedRate = 60000) // runs every 1 minute (60000 milliseconds)
    public void sendTaskReminders() {

        try {
            // Fetch tasks that need reminders sent (within the next 1 minute window)
            List<Task> tasks = taskService.getUpcomingRemindersForScheduler();

            System.out.println("Reminder Scheduler: Found " + tasks.size() + " tasks to remind");

            for (Task task : tasks) {
                try {
                    // Send reminder email
                    emailService.sendReminderEmail(
                            task.getUser().getEmail(),
                            task.getTitle()
                    );

                    // Mark reminder as sent
                    task.setReminderSent(true);
                    taskService.save(task);

                    System.out.println("Reminder sent for task: " + task.getTitle() + 
                                     " to user: " + task.getUser().getEmail());

                } catch (Exception e) {
                    // Log error but continue with other reminders
                    System.err.println("Failed to send reminder for task " + task.getId() + 
                                     ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error in reminder scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}