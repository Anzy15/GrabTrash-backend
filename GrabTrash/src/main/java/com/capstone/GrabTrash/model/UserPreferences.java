package com.capstone.GrabTrash.model;

import com.google.cloud.firestore.annotation.PropertyName;

public class UserPreferences {
    private boolean collectionRequestNotifications;
    private boolean generalAnnouncements;
    private String preferredLanguage;
    private String timeZone;

    public UserPreferences() {
        // Default values
        this.collectionRequestNotifications = true;
        this.generalAnnouncements = true;
        this.preferredLanguage = "en";
        this.timeZone = "UTC";
    }

    @PropertyName("collectionRequestNotifications")
    public boolean isCollectionRequestNotifications() {
        return collectionRequestNotifications;
    }

    @PropertyName("collectionRequestNotifications")
    public void setCollectionRequestNotifications(boolean collectionRequestNotifications) {
        this.collectionRequestNotifications = collectionRequestNotifications;
    }

    @PropertyName("generalAnnouncements")
    public boolean isGeneralAnnouncements() {
        return generalAnnouncements;
    }

    @PropertyName("generalAnnouncements")
    public void setGeneralAnnouncements(boolean generalAnnouncements) {
        this.generalAnnouncements = generalAnnouncements;
    }
    //updated lage ni di man ka motuo

   
} 