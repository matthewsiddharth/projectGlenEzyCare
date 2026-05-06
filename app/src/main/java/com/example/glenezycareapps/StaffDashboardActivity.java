package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StaffDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dash_board);

        Button btnCallQueue = findViewById(R.id.btnCallQueue);

        btnCallQueue.setOnClickListener(v -> startActivity(new Intent(
                StaffDashboardActivity.this,
                QueueCallingActivity.class)));
    }
}