// ==============================
// QueueStatusActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class QueueStatusActivity extends AppCompatActivity {

    TextView tvCurrentQueue, tvYourQueueNumber, tvYourQueueStatus;
    TextView tvYourDoctor, tvYourSpecialty, tvYourFloor;
    CardView cardYourTicket;
    android.widget.ImageView btnBack;
    android.widget.Button btnRefreshQueue;

    DatabaseReference queueRef;
    DatabaseReference userRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_status);

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);
        tvYourQueueNumber = findViewById(R.id.tvYourQueueNumber);
        tvYourQueueStatus = findViewById(R.id.tvYourQueueStatus);
        tvYourDoctor = findViewById(R.id.tvYourDoctor);
        tvYourSpecialty = findViewById(R.id.tvYourSpecialty);
        tvYourFloor = findViewById(R.id.tvYourFloor);
        
        cardYourTicket = findViewById(R.id.cardYourTicket);
        btnBack = findViewById(R.id.btnBack);
        btnRefreshQueue = findViewById(R.id.btnRefreshQueue);

        mAuth = FirebaseAuth.getInstance();
        queueRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("queue");

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnRefreshQueue != null) {
            btnRefreshQueue.setOnClickListener(v -> {
                Toast.makeText(this, "Status Refreshed", Toast.LENGTH_SHORT).show();
            });
        }

        loadNowServing();
        loadUserTicket();
    }

    private void loadNowServing() {
        queueRef.child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer current = snapshot.getValue(Integer.class);
                if (current != null) {
                    tvCurrentQueue.setText("Q" + String.format("%03d", current));
                } else {
                    tvCurrentQueue.setText("Q000");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUserTicket() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);

        userRef.child("currentTicket").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String queueNumber = snapshot.child("queueNumber").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String doctor = snapshot.child("doctor").getValue(String.class);
                    String specialty = snapshot.child("specialty").getValue(String.class);
                    String floor = snapshot.child("floor").getValue(String.class);

                    if (queueNumber != null) {
                        cardYourTicket.setVisibility(View.VISIBLE);
                        tvYourQueueNumber.setText(queueNumber);
                        tvYourQueueStatus.setText("Status: " + (status != null ? status : "Waiting"));
                        
                        tvYourDoctor.setText("Doctor: " + (doctor != null ? doctor : "---"));
                        tvYourSpecialty.setText("Specialty: " + (specialty != null ? specialty : "---"));
                        tvYourFloor.setText("Location: " + (floor != null ? floor : "---"));
                    }
                } else {
                    cardYourTicket.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}