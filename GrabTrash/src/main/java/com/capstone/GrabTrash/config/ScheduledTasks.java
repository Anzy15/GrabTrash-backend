package com.capstone.GrabTrash.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.capstone.GrabTrash.service.NotificationService;

@Component
@EnableScheduling
public class ScheduledTasks {
    private final NotificationService notificationService;

    @Autowired
    public ScheduledTasks(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Run daily at 6:00 PM to send reminders for tomorrow's collections
    @Scheduled(cron = "0 0 18 * * *")
    public void sendEveningReminders() {
        notificationService.sendScheduleReminders();
    }

    // Run daily at 6:00 AM to send reminders for today's collections
    @Scheduled(cron = "0 0 6 * * *")
    public void sendMorningReminders() {
        notificationService.sendScheduleReminders();
    }
} 