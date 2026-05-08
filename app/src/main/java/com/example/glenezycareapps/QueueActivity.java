// ==============================
// QueueActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class QueueActivity extends AppCompatActivity {

    TextView tvQueueNumber, tvQueueStatus;
    Button btnGenerateQueue;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        tvQueueStatus = findViewById(R.id.tvQueueStatus);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnGenerateQueue = findViewById(R.id.btnGenerateQueue);

        queueRef = FirebaseDatabase.getInstance()
                .getReference("queue");

        btnGenerateQueue.setOnClickListener(v -> generateQueue());
    }

    private void generateQueue() {

        queueRef.child("currentQueue")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                int currentNumber = 1;

                                if(snapshot.exists()) {

                                    currentNumber =
                                            snapshot.getValue(Integer.class);
                                }

                                String queueNumber =
                                        "Q" + String.format("%03d",
                                                currentNumber);

                                tvQueueNumber.setText(queueNumber);
                                tvQueueStatus.setText("Status: Waiting");

                                HashMap<String, String> queueMap =
                                        new HashMap<>();

                                queueMap.put("queueNumber",
                                        queueNumber);

                                queueMap.put("status",
                                        "Waiting");

                                queueRef.child("tickets")
                                        .push()
                                        .setValue(queueMap);

                                queueRef.child("currentQueue")
                                        .setValue(currentNumber + 1);

                                Toast.makeText(
                                        QueueActivity.this,
                                        "Queue Ticket Generated",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        });
    }
}