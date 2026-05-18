// ==============================
// StaffManagementActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StaffManagementActivity extends AppCompatActivity {

    EditText etStaffName, etStaffEmail, etStaffRole;
    Button btnAddStaff;
    android.widget.ImageView btnBack;

    DatabaseReference staffRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);

        etStaffName = findViewById(R.id.etStaffName);
        etStaffEmail = findViewById(R.id.etStaffEmail);
        etStaffRole = findViewById(R.id.etStaffRole);

        btnAddStaff = findViewById(R.id.btnAddStaff);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Use the common database path
        staffRef = FirebaseDatabase
                .getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("staff");

        btnAddStaff.setOnClickListener(v -> addStaff());
    }

    private void addStaff() {
        String name = etStaffName.getText().toString().trim();
        String email = etStaffEmail.getText().toString().trim();
        String role = etStaffRole.getText().toString().trim().toLowerCase();

        if (name.isEmpty() || email.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!role.equals("staff") && !role.equals("admin")) {
            Toast.makeText(this, "Role must be 'staff' or 'admin'", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> staffMap = new HashMap<>();
        staffMap.put("fullName", name);
        staffMap.put("email", email);
        staffMap.put("role", role);

        // Push to a list of pre-approved staff
        staffRef.push().setValue(staffMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Staff invitation sent. The user can now register using this email and must verify it.", Toast.LENGTH_LONG).show();
                etStaffName.setText("");
                etStaffEmail.setText("");
                etStaffRole.setText("");
            } else {
                Toast.makeText(this, "Error adding staff: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}