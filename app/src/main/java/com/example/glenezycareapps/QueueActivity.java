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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

public class QueueActivity extends AppCompatActivity {

    TextView tvQueueNumber, tvQueueStatus;
    Button btnGenerateQueue;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;
    DatabaseReference userRef;
    FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();
        queueRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("queue");
        
        if (mAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(mAuth.getCurrentUser().getUid());
        }

        btnGenerateQueue.setOnClickListener(v -> generateQueue());
    }

    private void generateQueue() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to generate a ticket", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        queueRef.child("nextTicketNumber")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                int nextNumber = 1;

                                if(snapshot.exists()) {
                                    nextNumber = snapshot.getValue(Integer.class);
                                }

                                String queueNumber = "Q" + String.format("%03d", nextNumber);

                                tvQueueNumber.setText(queueNumber);
                                tvQueueStatus.setText("Status: Waiting");

                                HashMap<String, Object> queueMap = new HashMap<>();
                                queueMap.put("queueNumber", queueNumber);
                                queueMap.put("status", "Waiting");
                                queueMap.put("userId", userId);
                                queueMap.put("timestamp", ServerValue.TIMESTAMP);

                                // Push to global tickets
                                String ticketId = queueRef.child("tickets").push().getKey();
                                if (ticketId != null) {
                                    queueRef.child("tickets").child(ticketId).setValue(queueMap);
                                    
                                    // Also save a reference to the patient's own profile
                                    userRef.child("currentTicket").setValue(queueMap);
                                }

                                queueRef.child("nextTicketNumber").setValue(nextNumber + 1);

                                Toast.makeText(
                                        QueueActivity.this,
                                        "Queue Ticket Generated: " + queueNumber,
                                        Toast.LENGTH_SHORT).show();

                                // Automatically navigate to Status screen so they can track it
                                android.content.Intent intent = new android.content.Intent(QueueActivity.this, QueueStatusActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {
                                Toast.makeText(QueueActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}