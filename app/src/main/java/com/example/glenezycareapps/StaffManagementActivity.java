package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StaffManagementActivity extends AppCompatActivity {

    EditText etStaffName,
            etStaffEmail,
            etStaffRole;

    Button btnAddStaff,
            btnViewStaff,
            btnEditStaff,
            btnDeleteStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);

        etStaffName = findViewById(R.id.etStaffName);
        etStaffEmail = findViewById(R.id.etStaffEmail);
        etStaffRole = findViewById(R.id.etStaffRole);

        btnAddStaff = findViewById(R.id.btnAddStaff);
        btnViewStaff = findViewById(R.id.btnViewStaff);
        btnEditStaff = findViewById(R.id.btnEditStaff);
        btnDeleteStaff = findViewById(R.id.btnDeleteStaff);

        btnAddStaff.setOnClickListener(v -> {

            String name = etStaffName.getText().toString();
            String email = etStaffEmail.getText().toString();
            String role = etStaffRole.getText().toString();

            Toast.makeText(this,
                    "Staff Added Successfully",
                    Toast.LENGTH_SHORT).show();

        });

        btnViewStaff.setOnClickListener(v -> {

            Toast.makeText(this,
                    "Viewing Staff Records",
                    Toast.LENGTH_SHORT).show();

        });

        btnEditStaff.setOnClickListener(v -> {

            Toast.makeText(this,
                    "Staff Record Updated",
                    Toast.LENGTH_SHORT).show();

        });

        btnDeleteStaff.setOnClickListener(v -> {

            Toast.makeText(this,
                    "Staff Record Deleted",
                    Toast.LENGTH_SHORT).show();

        });
    }
}