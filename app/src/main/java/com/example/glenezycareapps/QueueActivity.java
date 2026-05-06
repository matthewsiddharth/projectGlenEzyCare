package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class QueueActivity extends AppCompatActivity {

    TextView tvQueueNumber, tvQueueStatus;
    Button btnGenerateQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        tvQueueStatus = findViewById(R.id.tvQueueStatus);

        btnGenerateQueue = findViewById(R.id.btnGenerateQueue);

        btnGenerateQueue.setOnClickListener(v -> {

            generateQueueTicket();

        });
    }

    private void generateQueueTicket() {

        Random random = new Random();

        int number = random.nextInt(900) + 100;

        String queueNumber = "Q" + number;

        tvQueueNumber.setText(queueNumber);

        tvQueueStatus.setText("Status: Waiting");

        Toast.makeText(this,
                "Queue Ticket Generated",
                Toast.LENGTH_SHORT).show();
    }
}