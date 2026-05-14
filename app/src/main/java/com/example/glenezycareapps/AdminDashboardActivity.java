package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    CardView btnManageStaff,
            btnManagePatients,
            btnManageAppointments,
            btnQueueControl,
            btnViewQueueStatus;
    ImageView btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnManageStaff = findViewById(R.id.btnManageStaff);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnManageAppointments = findViewById(R.id.btnManageAppointments);
        btnQueueControl = findViewById(R.id.btnQueueControl);
        btnViewQueueStatus = findViewById(R.id.btnViewQueueStatus);
        btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> showPopupMenu(v));

        btnManageStaff.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, StaffManagementActivity.class));
        });

        btnManagePatients.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, PatientRecordsActivity.class));
        });

        btnManageAppointments.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AppointmentManagementActivity.class));
        });

        btnQueueControl.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, QueueCallingActivity.class));
        });

        btnViewQueueStatus.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, QueueStatusActivity.class));
        });
    }

    private void showPopupMenu(android.view.View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }
}