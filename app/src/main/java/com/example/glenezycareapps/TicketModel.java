package com.example.glenezycareapps;

public class TicketModel {
    private String queueNumber;
    private String status;
    private String userId;
    private String specialty;
    private String doctor;
    private String floor;
    private long timestamp;

    public TicketModel() {
        // Required for Firebase
    }

    public TicketModel(String queueNumber, String status, String userId, String specialty, String doctor, String floor, long timestamp) {
        this.queueNumber = queueNumber;
        this.status = status;
        this.userId = userId;
        this.specialty = specialty;
        this.doctor = doctor;
        this.floor = floor;
        this.timestamp = timestamp;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(String queueNumber) {
        this.queueNumber = queueNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
