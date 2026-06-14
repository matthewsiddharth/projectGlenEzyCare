package com.example.glenezycareapps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StaffDashboardActivity extends AppCompatActivity {

    View btnCallQueue, btnPatientRecords, btnAppointments;
    View cardStaffCurrentQueue, cardStaffPatientsWaiting;
    LinearLayout btnLogout, llLiveQueueList, navDashboard, navQueue, navPatients;
    TextView tvStaffName, tvStaffCurrentQueue, tvStaffWaitingCount;
    ImageView ivStaffProfile, btnMenu, btnNotification;
    View vNotificationBadge;
    DrawerLayout drawerLayout;
    SwipeRefreshLayout swipeRefresh;

    DatabaseReference rootRef;
    private int currentNowServing = 0;
    private DataSnapshot lastTicketsSnapshot;
    private String staffSpecialty = null;
    private String staffRole = "";
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dash_board);

        btnCallQueue = findViewById(R.id.btnCallQueue);
        btnPatientRecords = findViewById(R.id.btnPatientRecords);
        btnAppointments = findViewById(R.id.btnAppointments);
        cardStaffCurrentQueue = findViewById(R.id.cardStaffCurrentQueue);
        cardStaffPatientsWaiting = findViewById(R.id.cardStaffPatientsWaiting);
        btnLogout = findViewById(R.id.btnLogout);
        navDashboard = findViewById(R.id.navDashboard);
        navQueue = findViewById(R.id.navQueue);
        navPatients = findViewById(R.id.navPatients);
        
        tvStaffName = findViewById(R.id.tvStaffName);
        ivStaffProfile = findViewById(R.id.ivStaffProfile);
        btnNotification = findViewById(R.id.btnNotification);
        vNotificationBadge = findViewById(R.id.vNotificationBadge);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        tvStaffCurrentQueue = findViewById(R.id.tvStaffCurrentQueue);
        tvStaffWaitingCount = findViewById(R.id.tvStaffWaitingCount);
        llLiveQueueList = findViewById(R.id.llLiveQueueList);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        fetchStaffData();
        listenForNotifications();
        requestNotificationPermission();

        swipeRefresh.setOnRefreshListener(this::reloadData);

        btnCallQueue.setOnClickListener(v -> {
            Intent intent = new Intent(this, QueueCallingActivity.class);
            intent.putExtra("specialty", staffSpecialty);
            startActivity(intent);
        });
        btnPatientRecords.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        btnAppointments.setOnClickListener(v -> startActivity(new Intent(this, AppointmentManagementActivity.class)));
        
        cardStaffCurrentQueue.setOnClickListener(v -> {
            Intent intent = new Intent(this, QueueCallingActivity.class);
            intent.putExtra("specialty", staffSpecialty);
            startActivity(intent);
        });
        cardStaffPatientsWaiting.setOnClickListener(v -> {
            Intent intent = new Intent(this, QueueCallingActivity.class);
            intent.putExtra("specialty", staffSpecialty);
            startActivity(intent);
        });

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        ivStaffProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        navDashboard.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        navQueue.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, QueueCallingActivity.class);
            intent.putExtra("specialty", staffSpecialty);
            startActivity(intent);
        });
        navPatients.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, PatientRecordsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void reloadData() {
        fetchStaffData();
        fetchRealTimeQueue();
        new android.os.Handler().postDelayed(() -> swipeRefresh.setRefreshing(false), 1500);
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
                    staffSpecialty = snapshot.child("specialty").getValue(String.class);
                    staffRole = snapshot.child("role").getValue(String.class);

                    if (name != null) tvStaffName.setText(name);
                    if (profilePic != null && !profilePic.isEmpty() && !isDestroyed()) {
                        Glide.with(StaffDashboardActivity.this).load(profilePic).placeholder(R.drawable.iconprofile).into(ivStaffProfile);
                    }
                    
                    // Start queue listener once specialty is known
                    fetchRealTimeQueue();
                    updateWaitingListUI();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchRealTimeQueue() {
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        // Check for daily reset before listening to nowServing
        rootRef.child("queue").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (staffSpecialty != null) {
                    String lastReset = snapshot.child("lastResetDate").child(staffSpecialty).getValue(String.class);
                    if (lastReset == null || !todayDate.equals(lastReset)) {
                        // New day detected by staff login. Reset for this specialty.
                        rootRef.child("queue").child("lastResetDate").child(staffSpecialty).setValue(todayDate);
                        rootRef.child("queue").child("nowServing").child(staffSpecialty).setValue(0);
                        rootRef.child("queue").child("nextTicketNumber").child(staffSpecialty).setValue(1);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Current Serving
        rootRef.child("queue").child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer current;
                if (staffSpecialty != null && snapshot.hasChild(staffSpecialty)) {
                    current = snapshot.child(staffSpecialty).getValue(Integer.class);
                } else if (snapshot.getValue() instanceof Integer) {
                    current = snapshot.getValue(Integer.class);
                } else {
                    current = 0;
                }

                currentNowServing = (current != null) ? current : 0;
                
                String prefix = (staffSpecialty != null) ? getPrefixForSpecialty(staffSpecialty) : "Q";
                
                if (currentNowServing > 0) {
                    tvStaffCurrentQueue.setText(prefix + String.format("%03d", currentNowServing));
                } else {
                    tvStaffCurrentQueue.setText("---");
                }
                updateWaitingListUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Patients Waiting List
        rootRef.child("queue").child("tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastTicketsSnapshot = snapshot;
                updateWaitingListUI();
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

    private void updateWaitingListUI() {
        if (lastTicketsSnapshot == null) return;

        llLiveQueueList.removeAllViews();
        int waitingCount = 0;
        int displayed = 0;

        for (DataSnapshot ds : lastTicketsSnapshot.getChildren()) {
            String qNumStr = ds.child("queueNumber").getValue(String.class);
            String ticketSpecialty = ds.child("specialty").getValue(String.class);
            String status = ds.child("status").getValue(String.class);

            if (qNumStr != null && qNumStr.length() >= 4) {
                try {
                    // Logic: 
                    // 1. Only show tickets matching their specialty. 
                    // 2. Only show tickets that are "Waiting"
                    // 3. Admin sees everything.
                    
                    boolean isMySpecialty = (staffRole != null && staffRole.equals("admin")) || 
                                           (staffSpecialty != null && staffSpecialty.equals(ticketSpecialty));

                    boolean isWaiting = "Waiting".equals(status);

                    if (isMySpecialty && isWaiting) {
                        waitingCount++;
                        if (displayed < 5) {
                            String userId = ds.child("userId").getValue(String.class);
                            addQueueItemToView(qNumStr, userId);
                            displayed++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        tvStaffWaitingCount.setText(String.valueOf(waitingCount));

        if (waitingCount == 0) {
            TextView empty = new TextView(StaffDashboardActivity.this);
            empty.setText("No patients waiting");
            empty.setPadding(30, 30, 30, 30);
            empty.setGravity(android.view.Gravity.CENTER);
            llLiveQueueList.addView(empty);
        }
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

    private void listenForNotifications() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef.child("notifications").child(uid).addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (!isInitialLoad) {
                    NotificationModel notif = snapshot.getValue(NotificationModel.class);
                    if (notif != null) {
                        NotificationHelper.showNotification(StaffDashboardActivity.this, notif.getTitle(), notif.getMessage());
                        vNotificationBadge.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        rootRef.child("notifications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInitialLoad = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationModel notif = ds.getValue(NotificationModel.class);
                    if (notif != null && !notif.isRead()) {
                        vNotificationBadge.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}