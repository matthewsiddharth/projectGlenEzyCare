package com.example.glenezycareapps;

public class NotificationModel {
    private String id;
    private String userId;
    private String title;
    private String message;
    private long timestamp;
    private String type;
    private String appointmentId;
    private boolean isRead;
    private String actionStatus; // "confirmed", "declined", or null

    public NotificationModel() {}

    public NotificationModel(String id, String userId, String title, String message, long timestamp, String type, String appointmentId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.appointmentId = appointmentId;
        this.isRead = false;
        this.actionStatus = null;
    }

    public String getActionStatus() { return actionStatus; }
    public void setActionStatus(String actionStatus) { this.actionStatus = actionStatus; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
