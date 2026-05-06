package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PatientHomeActivity extends AppCompatActivity {

    Button btnQueue, btnQueueStatus,
            btnAppointment, btnHistory;
    ImageView btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        btnQueue = findViewById(R.id.btnQueue);
        btnQueueStatus = findViewById(R.id.btnQueueStatus);
        btnAppointment = findViewById(R.id.btnAppointment);
        btnHistory = findViewById(R.id.btnHistory);
        btnProfile = findViewById(R.id.btnProfile);

        btnQueue.setOnClickListener(v -> {
            startActivity(new Intent(
                    PatientHomeActivity.this,
                    QueueActivity.class));
        });

        btnQueueStatus.setOnClickListener(v -> {

            startActivity(new Intent(
                    PatientHomeActivity.this,
                    QueueStatusActivity.class));

        });

        btnAppointment.setOnClickListener(v -> {

            startActivity(new Intent(
                    PatientHomeActivity.this,
                    AppointmentActivity.class));

        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show();
            // Once ProfileActivity is created, use:
            // startActivity(new Intent(PatientHomeActivity.this, ProfileActivity.class));
        });
    }
}
