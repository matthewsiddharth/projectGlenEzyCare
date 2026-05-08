package com.example.glenezycareapps;

public class UserModel {
    private String fullName;
    private String email;
    private String role;
    private String userId;

    public UserModel() {
        // Required for Firebase
    }

    public UserModel(String fullName, String email, String role, String userId) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
