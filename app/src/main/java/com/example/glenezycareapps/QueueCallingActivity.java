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

        queueRef = FirebaseDatabase.getInstance()
                .getReference("queue");

        btnCallNext.setOnClickListener(v -> callNextQueue());
        
        if (btnCompleteQueue != null) {
            btnCompleteQueue.setOnClickListener(v -> {
                queueRef.child("currentQueue").setValue(1);
                queueRef.child("tickets").removeValue();
                android.widget.Toast.makeText(this, "Session Completed. Queue Reset.", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        loadCurrentQueue();
    }

    private void loadCurrentQueue() {

        queueRef.child("currentQueue")
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
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        });
    }

    private void callNextQueue() {
        queueRef.child("currentQueue")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Integer current = task.getResult().getValue(Integer.class);
                        if (current != null) {
                            queueRef.child("currentQueue").setValue(current + 1);
                            android.widget.Toast.makeText(this, "Calling Next: Q" + String.format("%03d", current + 1), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If it doesn't exist yet, start at 1
                        queueRef.child("currentQueue").setValue(1);
                    }
                });
    }
}