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

    private String currentTicketSpecialty = null;

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

        setupNowServingListener();
        loadUserTicket();
    }

    private void setupNowServingListener() {
        queueRef.child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer current = 0;
                String prefix = "Q";

                if (currentTicketSpecialty != null && snapshot.hasChild(currentTicketSpecialty)) {
                    current = snapshot.child(currentTicketSpecialty).getValue(Integer.class);
                    prefix = getPrefixForSpecialty(currentTicketSpecialty);
                } else if (snapshot.getValue() instanceof Integer) {
                    current = snapshot.getValue(Integer.class);
                }
                
                if (current != null && current > 0) {
                    tvCurrentQueue.setText(prefix + String.format("%03d", current));
                } else {
                    tvCurrentQueue.setText(prefix + "000");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getPrefixForSpecialty(String specialty) {
        if (specialty == null) return "Q";
        switch (specialty) {
            case "Cardiology": return "CAR";
            case "ENT (Otorhinolaryngology)": return "ENT";
            case "Orthopedic Surgery": return "ORT";
            case "Dermatology": return "DER";
            case "Pediatrics": return "PED";
            case "Obstetrics & Gynecology": return "OBS";
            case "Ophthalmology": return "OPH";
            case "Gastroenterology": return "GAS";
            case "Neurology": return "NEU";
            case "Psychiatry": return "PSY";
            case "Dentistry": return "DEN";
            case "General Surgery": return "GEN";
            default: return "Q";
        }
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

                    currentTicketSpecialty = specialty;

                    if (queueNumber != null) {
                        cardYourTicket.setVisibility(View.VISIBLE);
                        tvYourQueueNumber.setText(queueNumber);
                        tvYourQueueStatus.setText("Status: " + (status != null ? status : "Waiting"));
                        
                        tvYourDoctor.setText("Doctor: " + (doctor != null ? doctor : "---"));
                        tvYourSpecialty.setText("Specialty: " + (specialty != null ? specialty : "---"));
                        tvYourFloor.setText("Location: " + (floor != null ? floor : "---"));
                    }
                } else {
                    currentTicketSpecialty = null;
                    cardYourTicket.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}