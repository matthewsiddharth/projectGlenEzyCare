// ==============================
// RegisterActivity.java
// ==============================

package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etRoleCode;
    Button btnRegister;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRoleCode = findViewById(R.id.etRoleCode);

        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase
                .getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

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
        String roleCode = etRoleCode.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {

            Toast.makeText(RegisterActivity.this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {

                        if (mAuth.getCurrentUser() != null) {
                            String userId = mAuth.getCurrentUser().getUid();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("fullName", name);
                            userMap.put("email", email);

                            // Role logic based on secret code
                            String role = "patient"; // Default
                            if (roleCode.equals("ADMIN123")) {
                                role = "admin";
                            } else if (roleCode.equals("STAFF123")) {
                                role = "staff";
                            }

                            userMap.put("role", role);
                            String finalRole = role;

                            databaseReference.child(userId).setValue(userMap).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this,
                                            "Registration Successful as " + finalRole,
                                            Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                            "Database Error: " + (dbTask.getException() != null ? dbTask.getException().getMessage() : "Failed to save user data"),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                task.getException() != null ? task.getException().getMessage() : "Registration failed",
                                Toast.LENGTH_LONG).show();
                    }

                });
    }
}