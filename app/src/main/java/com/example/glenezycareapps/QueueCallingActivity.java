// ==============================
// QueueCallingActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class QueueCallingActivity extends AppCompatActivity {

    TextView tvCurrentQueue;

    Button btnCallNext, btnCompleteQueue;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_calling);

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);

        btnCallNext = findViewById(R.id.btnCallNext);
        btnCompleteQueue = findViewById(R.id.btnCompleteQueue);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        queueRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("queue");

        btnCallNext.setOnClickListener(v -> callNextQueue());
        
        if (btnCompleteQueue != null) {
            btnCompleteQueue.setOnClickListener(v -> {
                queueRef.child("nextTicketNumber").setValue(1);
                queueRef.child("nowServing").setValue(0);
                queueRef.child("tickets").removeValue();
                android.widget.Toast.makeText(this, "Session Completed. Queue Reset.", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        loadCurrentQueue();
    }

    private void loadCurrentQueue() {

        queueRef.child("nowServing")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                Integer current =
                                        snapshot.getValue(Integer.class);

                                if(current != null) {

                                    tvCurrentQueue.setText(
                                            "Q" + String.format("%03d",
                                                    current));
                                } else {
                                    tvCurrentQueue.setText("Q000");
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        });
    }

    private void callNextQueue() {
        queueRef.child("nowServing")
                .get()
                .addOnCompleteListener(task -> {
                    int currentServing = 0;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Integer val = task.getResult().getValue(Integer.class);
                        if (val != null) currentServing = val;
                    }
                    
                    int nextToServe = currentServing + 1;
                    
                    // Optional: Check if nextToServe actually exists in tickets or hasn't exceeded nextTicketNumber
                    queueRef.child("nowServing").setValue(nextToServe);
                    android.widget.Toast.makeText(this, "Calling: Q" + String.format("%03d", nextToServe), android.widget.Toast.LENGTH_SHORT).show();
                });
    }
}