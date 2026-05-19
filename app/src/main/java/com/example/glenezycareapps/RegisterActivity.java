// ==============================
// RegisterActivity.java
// ==============================

package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
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

        usersRef = FirebaseDatabase
                .getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");
        
        staffCheckRef = FirebaseDatabase
                .getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("staff");

        btnRegister.setOnClickListener(v -> registerUser());
        
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email is in the pre-approved staff list
        staffCheckRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = "patient"; // Default role
                String specialty = "";
                if (snapshot.exists()) {
                    // This email is pre-approved for a staff/admin role
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        role = ds.child("role").getValue(String.class);
                        specialty = ds.child("specialty").getValue(String.class);
                        if (role == null) role = "staff";
                        if (specialty == null) specialty = "";
                    }
                }
                
                final String finalRole = role;
                final String finalSpecialty = specialty;
                createAuthUser(email, password, name, finalRole, finalSpecialty);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createAuthUser(email, password, name, "patient", "");
            }
        });
    }

    private void createAuthUser(String email, String password, String name, String role, String specialty) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            String userId = mAuth.getCurrentUser().getUid();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("fullName", name);
                            userMap.put("email", email);
                            userMap.put("role", role);
                            if (!specialty.isEmpty()) {
                                userMap.put("specialty", specialty);
                            }

                            usersRef.child(userId).setValue(userMap).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    if (role.equals("staff") || role.equals("admin")) {
                                        notifyAdminsOfNewStaff(name, role);
                                    }
                                    sendVerificationEmail();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Database Error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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