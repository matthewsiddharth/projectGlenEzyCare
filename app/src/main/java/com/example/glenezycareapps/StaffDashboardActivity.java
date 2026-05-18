package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StaffDashboardActivity extends AppCompatActivity {

    View btnCallQueue, btnPatientRecords, btnAppointments;
    LinearLayout btnLogout, llLiveQueueList;
    TextView tvStaffName, tvStaffCurrentQueue, tvStaffWaitingCount;
    ImageView ivStaffProfile, btnMenu;
    DrawerLayout drawerLayout;

    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dash_board);

        btnCallQueue = findViewById(R.id.btnCallQueue);
        btnPatientRecords = findViewById(R.id.btnPatientRecords);
        btnAppointments = findViewById(R.id.btnAppointments);
        btnLogout = findViewById(R.id.btnLogout);
        
        tvStaffName = findViewById(R.id.tvStaffName);
        ivStaffProfile = findViewById(R.id.ivStaffProfile);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        tvStaffCurrentQueue = findViewById(R.id.tvStaffCurrentQueue);
        tvStaffWaitingCount = findViewById(R.id.tvStaffWaitingCount);
        llLiveQueueList = findViewById(R.id.llLiveQueueList);

        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        fetchStaffData();
        fetchRealTimeQueue();

        btnCallQueue.setOnClickListener(v -> startActivity(new Intent(this, QueueCallingActivity.class)));
        btnPatientRecords.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        btnAppointments.setOnClickListener(v -> startActivity(new Intent(this, AppointmentManagementActivity.class)));
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        ivStaffProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchStaffData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String profilePic = snapshot.child("profilePic").getValue(String.class);
                    if (name != null) tvStaffName.setText(name);
                    if (profilePic != null && !profilePic.isEmpty() && !isDestroyed()) {
                        Glide.with(StaffDashboardActivity.this).load(profilePic).placeholder(R.drawable.iconprofile).into(ivStaffProfile);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchRealTimeQueue() {
        // Current Serving
        rootRef.child("queue").child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer current = snapshot.getValue(Integer.class);
                if (current != null && current > 0) {
                    tvStaffCurrentQueue.setText("Q" + String.format("%03d", current));
                } else {
                    tvStaffCurrentQueue.setText("---");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Patients Waiting List
        rootRef.child("queue").child("tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvStaffWaitingCount.setText(String.valueOf(count));

                llLiveQueueList.removeAllViews();
                if (count == 0) {
                    TextView empty = new TextView(StaffDashboardActivity.this);
                    empty.setText("No patients waiting");
                    empty.setPadding(30, 30, 30, 30);
                    llLiveQueueList.addView(empty);
                } else {
                    int displayed = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (displayed >= 5) break;
                        String qNum = ds.child("queueNumber").getValue(String.class);
                        String userId = ds.child("userId").getValue(String.class);
                        
                        addQueueItemToView(qNum, userId);
                        displayed++;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addQueueItemToView(String number, String userId) {
        View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llLiveQueueList, false);
        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);

        text1.setText(number);
        text1.setTextColor(getResources().getColor(R.color.staff_primary));
        text1.setTextSize(16);
        
        // Fetch patient name
        rootRef.child("users").child(userId).child("fullName").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                text2.setText(task.getResult().getValue(String.class));
            } else {
                text2.setText("Unknown Patient");
            }
        });

        llLiveQueueList.addView(view);
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFFF1F1F1);
        llLiveQueueList.addView(divider);
    }
}