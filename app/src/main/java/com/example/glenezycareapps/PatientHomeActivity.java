package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PatientHomeActivity extends AppCompatActivity {

    View btnQueue, btnQueueStatus,
            btnAppointment, btnHistory;
    ImageView btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        btnQueue = findViewById(R.id.btnQueue);
        btnQueueStatus = findViewById(R.id.btnQueueStatus);
        btnAppointment = findViewById(R.id.btnAppointment);
        btnHistory = findViewById(R.id.btnHistory);
        btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(this::showPopupMenu);

        btnQueue.setOnClickListener(v -> {
            startActivity(new Intent(PatientHomeActivity.this, QueueActivity.class));
        });

        btnQueueStatus.setOnClickListener(v -> {
            startActivity(new Intent(PatientHomeActivity.this, QueueStatusActivity.class));
        });

        btnAppointment.setOnClickListener(v -> {
            startActivity(new Intent(PatientHomeActivity.this, AppointmentActivity.class));
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(PatientHomeActivity.this, AppointmentHistoryActivity.class));
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                return true;
            } else if (id == R.id.menu_profile) {
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