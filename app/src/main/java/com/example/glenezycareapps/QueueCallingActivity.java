package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QueueCallingActivity extends AppCompatActivity {

    TextView tvCurrentQueue;

    Button btnCallNext, btnCompleteQueue;

    int currentQueue = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_calling);

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);

        btnCallNext = findViewById(R.id.btnCallNext);
        btnCompleteQueue = findViewById(R.id.btnCompleteQueue);

        btnCallNext.setOnClickListener(v -> {

            currentQueue++;

            String queueNumber =
                    "Q" + String.format("%03d", currentQueue);

            tvCurrentQueue.setText(queueNumber);

            Toast.makeText(this,
                    "Next Queue Called",
                    Toast.LENGTH_SHORT).show();

        });

        btnCompleteQueue.setOnClickListener(v -> {

            Toast.makeText(this,
                    "Queue Completed",
                    Toast.LENGTH_SHORT).show();

        });
    }
}