// ==============================
// QueueCallingActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class QueueCallingActivity extends AppCompatActivity {

    TextView tvCurrentQueue, tvCallingDoctor, tvCallingSpecialty;
    Button btnCallNext, btnCompleteQueue;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_calling);

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);
        tvCallingDoctor = findViewById(R.id.tvCallingDoctor);
        tvCallingSpecialty = findViewById(R.id.tvCallingSpecialty);

        btnCallNext = findViewById(R.id.btnCallNext);
        btnCompleteQueue = findViewById(R.id.btnCompleteQueue);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        queueRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("queue");

        btnCallNext.setOnClickListener(v -> callNextQueue());
        
        btnCompleteQueue.setOnClickListener(v -> {
            queueRef.child("nextTicketNumber").setValue(1);
            queueRef.child("nowServing").setValue(0);
            queueRef.child("tickets").removeValue();
            Toast.makeText(this, "Session Completed. Queue Reset.", Toast.LENGTH_SHORT).show();
        });

        loadCurrentQueueStatus();
    }

    private void loadCurrentQueueStatus() {
        queueRef.child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer current = snapshot.getValue(Integer.class);
                if (current != null && current > 0) {
                    String queueNum = "Q" + String.format("%03d", current);
                    tvCurrentQueue.setText(queueNum);
                    fetchTicketDetails(queueNum);
                } else {
                    tvCurrentQueue.setText("Q000");
                    tvCallingDoctor.setText("No one being served");
                    tvCallingSpecialty.setText("---");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchTicketDetails(String queueNum) {
        queueRef.child("tickets").orderByChild("queueNumber").equalTo(queueNum)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String doctor = child.child("doctor").getValue(String.class);
                                String specialty = child.child("specialty").getValue(String.class);
                                
                                tvCallingDoctor.setText("Serving for: " + (doctor != null ? doctor : "---"));
                                tvCallingSpecialty.setText("Dept: " + (specialty != null ? specialty : "---"));
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void callNextQueue() {
        queueRef.child("nowServing").get().addOnCompleteListener(task -> {
            int currentServing = 0;
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer val = task.getResult().getValue(Integer.class);
                if (val != null) currentServing = val;
            }
            
            int nextToServe = currentServing + 1;
            
            // Check if there is actually a next ticket before calling
            queueRef.child("nextTicketNumber").get().addOnCompleteListener(nextTask -> {
                if (nextTask.isSuccessful()) {
                    Integer nextAvailable = nextTask.getResult().getValue(Integer.class);
                    if (nextAvailable != null && nextToServe < nextAvailable) {
                        queueRef.child("nowServing").setValue(nextToServe);
                        Toast.makeText(this, "Calling: Q" + String.format("%03d", nextToServe), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No more patients in the queue", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}