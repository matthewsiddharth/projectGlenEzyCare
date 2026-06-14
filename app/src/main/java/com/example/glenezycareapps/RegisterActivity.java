package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    Button btnRegister;

    FirebaseAuth mAuth;
    DatabaseReference usersRef, staffCheckRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        // Use explicit URL for consistency
        String dbUrl = "https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/";
        usersRef = FirebaseDatabase.getInstance(dbUrl).getReference("users");
        staffCheckRef = FirebaseDatabase.getInstance(dbUrl).getReference("staff");

        btnRegister.setOnClickListener(v -> registerUser());
        
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Create the Auth account first. This authenticates the user so they can read the staff list.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            String userId = mAuth.getCurrentUser().getUid();
                            Log.d("RegisterDebug", "Auth created for UID: " + userId);
                            checkStaffListAndCreateProfile(userId, email, name);
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e("RegisterDebug", "Auth creation failed: " + error);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkStaffListAndCreateProfile(String userId, String email, String name) {
        Log.d("RegisterDebug", "Checking staff list for: " + email);
        
        staffCheckRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = "patient";
                String specialty = "";

                if (snapshot.exists()) {
                    Log.d("RegisterDebug", "Staff match found!");
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        role = ds.child("role").getValue(String.class);
                        specialty = ds.child("specialty").getValue(String.class);
                        if (role == null) role = "staff";
                        if (specialty == null) specialty = "";
                    }
                } else {
                    Log.d("RegisterDebug", "No staff match found. Assigning patient role.");
                }

                saveUserProfile(userId, name, email, role, specialty);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RegisterDebug", "Staff check cancelled: " + error.getMessage());
                // Fallback to patient if database read fails
                saveUserProfile(userId, name, email, "patient", "");
            }
        });
    }

    private void saveUserProfile(String userId, String name, String email, String role, String specialty) {
        HashMap<String, String> userMap = new HashMap<>();
        String finalName = name;
        if ("staff".equals(role) && !name.toLowerCase().startsWith("dr. ")) {
            finalName = "Dr. " + name;
        }

        userMap.put("fullName", finalName);
        userMap.put("email", email);
        userMap.put("role", role);
        if (!specialty.isEmpty()) {
            userMap.put("specialty", specialty);
        }

        usersRef.child(userId).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("RegisterDebug", "Profile saved successfully with role: " + role);
                if ("staff".equals(role) || "admin".equals(role)) {
                    notifyAdminsOfNewStaff(name, role);
                }
                sendVerificationEmail();
            } else {
                Log.e("RegisterDebug", "Failed to save profile: " + task.getException().getMessage());
                Toast.makeText(RegisterActivity.this, "Database Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void notifyAdminsOfNewStaff(String staffName, String role) {
        usersRef.orderByChild("role").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                    String adminId = adminSnapshot.getKey();
                    if (adminId != null) {
                        DatabaseReference notifRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("notifications").child(adminId);
                        String notifId = notifRef.push().getKey();
                        if (notifId != null) {
                            NotificationModel notification = new NotificationModel(
                                    notifId,
                                    adminId,
                                    "New Staff Registered",
                                    staffName + " has registered as " + role + ".",
                                    System.currentTimeMillis(),
                                    "staff_registration",
                                    null
                            );
                            notifRef.child(notifId).setValue(notification);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendVerificationEmail() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(verifyTask -> {
                        if (verifyTask.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Account created. Please check your email for a verification link before logging in.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email failed to send.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        
                        mAuth.signOut();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
        }
    }
}
