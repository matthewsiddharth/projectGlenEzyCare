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

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvForgotPassword, tvRegister;
    SwitchMaterial switchRole;
    
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        switchRole = findViewById(R.id.switchRole);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        btnLogin.setOnClickListener(v -> loginUser());
        
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        switchRole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btnLogin.setBackgroundResource(R.drawable.button_staff_gradient);
            } else {
                btnLogin.setBackgroundResource(R.drawable.button_gradient);
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
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            checkUserRole(mAuth.getCurrentUser().getUid());
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                
                String role = "patient";
                if (snapshot.exists() && snapshot.hasChild("role")) {
                    role = String.valueOf(snapshot.child("role").getValue()).trim().toLowerCase();
                }

                boolean isAdminStaffMode = switchRole.isChecked();
                Log.d("LoginDebug", "UID: " + userId);
                Log.d("LoginDebug", "Detected role from DB: [" + role + "]");
                Log.d("LoginDebug", "Toggle is ON (Staff/Admin mode): " + isAdminStaffMode);

                // Requirement: Patients and Staff must verify their email. Admins are exempt.
                if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
                    if (!role.contains("admin")) {
                        Toast.makeText(LoginActivity.this, "Your account is not verified. Please check your email for the verification link.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }
                }
                
                Intent intent = null;

                if (isAdminStaffMode) {
                    // Trying to login as Staff/Admin
                    if (role.contains("admin")) {
                        Log.d("LoginDebug", "Redirecting to Admin Dashboard");
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    } else if (role.contains("staff")) {
                        Log.d("LoginDebug", "Redirecting to Staff Dashboard");
                        intent = new Intent(LoginActivity.this, StaffDashboardActivity.class);
                    } else {
                        Log.e("LoginDebug", "Access Denied: Toggle ON but role is " + role);
                        Toast.makeText(LoginActivity.this, "Access Denied: Your account role is '" + role + "'. You are not authorized for Staff/Admin access.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Trying to login as Patient
                    if (role.contains("patient")) {
                        Log.d("LoginDebug", "Redirecting to Patient Home");
                        intent = new Intent(LoginActivity.this, PatientHomeActivity.class);
                    } else {
                        Log.e("LoginDebug", "Access Denied: Toggle OFF but role is " + role);
                        // User is staff/admin but forgot to flip the switch
                        Toast.makeText(LoginActivity.this, "Access Denied: You have a '" + role + "' account. Please use the Staff/Admin toggle to login.", Toast.LENGTH_LONG).show();
                    }
                }

                if (intent != null) {
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
