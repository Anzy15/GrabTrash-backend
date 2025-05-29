package com.capstone.GrabTrash.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capstone.GrabTrash.service.CollectionScheduleService;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

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