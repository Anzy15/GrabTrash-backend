package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String fcmToken;
    private String title;
    private String body;
    private Object data;  // Optional data payload
} 