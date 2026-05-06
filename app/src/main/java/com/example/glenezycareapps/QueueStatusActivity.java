package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QueueStatusActivity extends AppCompatActivity {

    TextView tvCurrentQueue;
    Button btnRefreshQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_status);

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);
        btnRefreshQueue = findViewById(R.id.btnRefreshQueue);

        btnRefreshQueue.setOnClickListener(v -> {

            tvCurrentQueue.setText("Q002");

            Toast.makeText(this,
                    "Queue Updated",
                    Toast.LENGTH_SHORT).show();

        });
    }
}