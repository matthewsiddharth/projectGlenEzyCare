package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class StaffDashboardActivity extends AppCompatActivity {

    CardView btnViewQueue, btnCallQueue, btnPatientRecords, btnAppointments;
    ImageView btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dash_board);

        btnViewQueue = findViewById(R.id.btnViewQueue);
        btnCallQueue = findViewById(R.id.btnCallQueue);
        btnPatientRecords = findViewById(R.id.btnPatientRecords);
        btnAppointments = findViewById(R.id.btnAppointments);
        btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(this::showPopupMenu);

        btnViewQueue.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboardActivity.this, QueueStatusActivity.class));
        });

        btnCallQueue.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboardActivity.this, QueueCallingActivity.class));
        });

        btnPatientRecords.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboardActivity.this, PatientRecordsActivity.class));
        });

        btnAppointments.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboardActivity.this, AppointmentManagementActivity.class));
        });
    }

    private void showPopupMenu(View view) {
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