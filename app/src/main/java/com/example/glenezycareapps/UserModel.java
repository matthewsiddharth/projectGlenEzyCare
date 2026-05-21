package com.example.glenezycareapps;

public class UserModel {
    private String fullName;
    private String email;
    private String role;
    private String userId;
    private String phone;
    private String profilePic;
    private String specialty;

    public UserModel() {
        // Required for Firebase
    }

    public UserModel(String fullName, String email, String role, String userId, String phone, String profilePic, String specialty) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.phone = phone;
        this.profilePic = profilePic;
        this.specialty = specialty;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
