package com.capstone.GrabTrash.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.capstone.GrabTrash.service.CollectionScheduleService;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulingConfig {

    @Autowired
    private CollectionScheduleService collectionScheduleService;

    /**
     * Send daily collection reminders at 6:00 AM every day
     * cron format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void sendDailyCollectionReminders() {
        log.info("Running scheduled task: sending daily collection reminders");
        collectionScheduleService.sendTodayCollectionReminders();
    }
} 