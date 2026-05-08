package com.example.glenezycareapps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginDetailsActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvForgotPassword;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_details);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        btnLogin.setOnClickListener(v -> loginUser());
        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        checkUserRole(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        Log.d("LoginDebug", "Checking role for UID: " + userId);
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    Log.d("LoginDebug", "User data found. Role: " + role);
                    
                    Intent intent = null;
                    if (role != null) {
                        String cleanRole = role.trim().toLowerCase();
                        if (cleanRole.equals("admin")) {
                            intent = new Intent(LoginDetailsActivity.this, AdminDashboardActivity.class);
                        } else if (cleanRole.equals("staff")) {
                            intent = new Intent(LoginDetailsActivity.this, StaffDashboardActivity.class);
                        } else if (cleanRole.equals("patient")) {
                            intent = new Intent(LoginDetailsActivity.this, PatientHomeActivity.class);
                        }
                    }

                    if (intent != null) {
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("LoginDebug", "Role not recognized: " + role);
                        Toast.makeText(LoginDetailsActivity.this, "Error: Unknown role (" + role + ")", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("LoginDebug", "No user data found in database for UID: " + userId);
                    Toast.makeText(LoginDetailsActivity.this, "User data not found in database.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Log.e("LoginDebug", "Database error: " + error.getMessage());
                Toast.makeText(LoginDetailsActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}