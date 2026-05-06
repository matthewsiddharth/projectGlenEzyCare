package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    Button btnManageStaff,
            btnManagePatients,
            btnManageAppointments,
            btnQueueControl,
            btnViewQueueStatus,
            btnAdminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnManageStaff = findViewById(R.id.btnManageStaff);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnManageAppointments = findViewById(R.id.btnManageAppointments);
        btnQueueControl = findViewById(R.id.btnQueueControl);
        btnViewQueueStatus = findViewById(R.id.btnViewQueueStatus);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        btnManageStaff.setOnClickListener(v -> {

            startActivity(new Intent(
                    AdminDashboardActivity.this,
                    StaffManagementActivity.class));

        });

        btnQueueControl.setOnClickListener(v -> {

            startActivity(new Intent(
                    AdminDashboardActivity.this,
                    QueueCallingActivity.class));

        });

        btnViewQueueStatus.setOnClickListener(v -> {

            startActivity(new Intent(
                    AdminDashboardActivity.this,
                    QueueStatusActivity.class));

        });
    }
}