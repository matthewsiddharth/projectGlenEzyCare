package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientHomeActivity extends AppCompatActivity {

    View btnQueue, btnQueueStatus, btnAppointment, btnHistory;
    TextView tvUserName, tvGreeting, tvHomeQueueNum, tvWaitTime, tvHomeConsultationRoom;
    ImageView btnMenu, ivProfileTop;
    BottomNavigationView bottomNav;
    ProgressBar pbWait;
    
    DatabaseReference rootRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        btnQueue = findViewById(R.id.btnQueue);
        btnQueueStatus = findViewById(R.id.btnQueueStatus);
        btnAppointment = findViewById(R.id.btnAppointment);
        btnHistory = findViewById(R.id.btnHistory);
        btnMenu = findViewById(R.id.btnMenu);
        ivProfileTop = findViewById(R.id.ivProfileTop);
        tvUserName = findViewById(R.id.tvUserName);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvHomeQueueNum = findViewById(R.id.tvHomeQueueNum);
        tvWaitTime = findViewById(R.id.tvWaitTime);
        tvHomeConsultationRoom = findViewById(R.id.tvHomeConsultationRoom);
        pbWait = findViewById(R.id.pbWait);
        bottomNav = findViewById(R.id.bottomNav);

        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mAuth = FirebaseAuth.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
            trackNowServing();
        }

        btnQueue.setOnClickListener(v -> startActivity(new Intent(this, QueueActivity.class)));
        btnQueueStatus.setOnClickListener(v -> startActivity(new Intent(this, QueueStatusActivity.class)));
        btnAppointment.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, AppointmentHistoryActivity.class)));

        ivProfileTop.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnMenu.setOnClickListener(v -> Toast.makeText(this, "Sidebar Menu coming soon", Toast.LENGTH_SHORT).show());

        setupBottomNav();
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String profilePic = snapshot.child("profilePic").getValue(String.class);
                    if (name != null) tvUserName.setText(name);
                    
                    if (profilePic != null && !profilePic.isEmpty() && !isDestroyed()) {
                        Glide.with(PatientHomeActivity.this).load(profilePic).placeholder(R.drawable.iconprofile).into(ivProfileTop);
                    }

                    // Active Queue
                    if (snapshot.hasChild("currentTicket")) {
                        String qNum = snapshot.child("currentTicket").child("queueNumber").getValue(String.class);
                        String specialty = snapshot.child("currentTicket").child("specialty").getValue(String.class);
                        String floor = snapshot.child("currentTicket").child("floor").getValue(String.class);
                        
                        if (qNum != null) tvHomeQueueNum.setText(qNum);
                        if (specialty != null && floor != null) {
                            tvHomeConsultationRoom.setText(specialty + " - " + floor);
                        }
                        calculateEstimatedWait(qNum);
                    } else {
                        tvHomeQueueNum.setText("None");
                        tvWaitTime.setText("--\nmin");
                        tvHomeConsultationRoom.setText("No active queue");
                        pbWait.setProgress(0);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void trackNowServing() {
        rootRef.child("queue").child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If nowServing changes, re-calculate wait time if ticket exists
                if (tvHomeQueueNum.getText() != null && !tvHomeQueueNum.getText().toString().equals("None")) {
                    calculateEstimatedWait(tvHomeQueueNum.getText().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateEstimatedWait(String myNumber) {
        if (myNumber == null || myNumber.length() < 2) return;
        
        try {
            int myNumInt = Integer.parseInt(myNumber.substring(1));
            rootRef.child("queue").child("nowServing").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    int nowServing = task.getResult().getValue(Integer.class);
                    int peopleAhead = myNumInt - nowServing;
                    
                    if (peopleAhead > 0) {
                        int waitTime = peopleAhead * 10; // Assume 10 mins per patient
                        tvWaitTime.setText(waitTime + "\nmin");
                        int progress = Math.max(0, 100 - (peopleAhead * 20)); // Dummy progress calc
                        pbWait.setProgress(progress);
                    } else if (peopleAhead == 0) {
                        tvWaitTime.setText("NOW");
                        pbWait.setProgress(100);
                    } else {
                        tvWaitTime.setText("Done");
                        pbWait.setProgress(100);
                    }
                }
            });
        } catch (Exception e) {
            tvWaitTime.setText("--\nmin");
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_appointments) {
                startActivity(new Intent(this, AppointmentHistoryActivity.class));
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}