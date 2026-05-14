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

    EditText etStaffName,
            etStaffEmail,
            etStaffRole;

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

        staffRef = FirebaseDatabase
                .getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("staff");

        btnAddStaff.setOnClickListener(v -> addStaff());
    }

    private void addStaff() {

        HashMap<String, String> staffMap =
                new HashMap<>();

        staffMap.put("fullName",
                etStaffName.getText().toString());

        staffMap.put("email",
                etStaffEmail.getText().toString());

        staffMap.put("role",
                etStaffRole.getText().toString());

        staffRef.push().setValue(staffMap);

        Toast.makeText(this,
                "Staff Added Successfully",
                Toast.LENGTH_SHORT).show();
    }
}