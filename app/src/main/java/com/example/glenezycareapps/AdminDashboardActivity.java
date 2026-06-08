package com.example.glenezycareapps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    View btnManageStaff, btnManagePatients, btnQueueControl, btnViewQueueStatus;
    View cardTotalPatients, cardActiveStaff, cardAppointmentsToday, cardQueueEfficiency;
    LinearLayout btnLogout, navDashboard, navStaff, navPatients, navAdmins, llDepartmentStats;
    TextView tvAdminName, tvTotalPatients, tvActiveStaff, tvAppointmentsToday, tvQueueEfficiency;
    ImageView ivAdminProfile, btnMenu, btnNotification;
    View vNotificationBadge;
    DrawerLayout drawerLayout;

    DatabaseReference rootRef;
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnManageStaff = findViewById(R.id.btnManageStaff);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnQueueControl = findViewById(R.id.btnQueueControl);
        btnViewQueueStatus = findViewById(R.id.btnViewQueueStatus);
        
        cardTotalPatients = findViewById(R.id.cardTotalPatients);
        cardActiveStaff = findViewById(R.id.cardActiveStaff);
        cardAppointmentsToday = findViewById(R.id.cardAppointmentsToday);
        cardQueueEfficiency = findViewById(R.id.cardQueueEfficiency);

        btnLogout = findViewById(R.id.btnLogout);
        navDashboard = findViewById(R.id.navDashboard);
        navStaff = findViewById(R.id.navStaff);
        navPatients = findViewById(R.id.navPatients);
        navAdmins = findViewById(R.id.navAdmins);
        
        tvAdminName = findViewById(R.id.tvAdminName);
        ivAdminProfile = findViewById(R.id.ivAdminProfile);
        btnNotification = findViewById(R.id.btnNotification);
        vNotificationBadge = findViewById(R.id.vNotificationBadge);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        
        tvTotalPatients = findViewById(R.id.tvTotalPatientsCount);
        tvActiveStaff = findViewById(R.id.tvActiveStaffCount);
        tvAppointmentsToday = findViewById(R.id.tvAppointmentsTodayCount);
        tvQueueEfficiency = findViewById(R.id.tvQueueEfficiencyValue);
        llDepartmentStats = findViewById(R.id.llDepartmentStats);

        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        fetchAdminData();
        fetchRealTimeStats();
        fetchDepartmentOverview();
        listenForNotifications();
        requestNotificationPermission();

        btnManageStaff.setOnClickListener(v -> startActivity(new Intent(this, StaffManagementActivity.class)));
        btnManagePatients.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        btnQueueControl.setOnClickListener(v -> startActivity(new Intent(this, QueueCallingActivity.class)));
        btnViewQueueStatus.setOnClickListener(v -> startActivity(new Intent(this, QueueStatusActivity.class)));

        cardTotalPatients.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        cardActiveStaff.setOnClickListener(v -> startActivity(new Intent(this, StaffListActivity.class)));
        cardAppointmentsToday.setOnClickListener(v -> startActivity(new Intent(this, AppointmentManagementActivity.class)));
        cardQueueEfficiency.setOnClickListener(v -> startActivity(new Intent(this, QueueStatusActivity.class)));

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ivAdminProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        navDashboard.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        navStaff.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, StaffListActivity.class));
        });
        navPatients.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, PatientRecordsActivity.class));
        });

        navAdmins.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AdminListActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchAdminData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String profilePic = snapshot.child("profilePic").getValue(String.class);
                    if (name != null) tvAdminName.setText(name);
                    if (profilePic != null && !profilePic.isEmpty() && !isDestroyed()) {
                        Glide.with(AdminDashboardActivity.this).load(profilePic).placeholder(R.drawable.iconprofile).into(ivAdminProfile);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchRealTimeStats() {
        // Total Patients Count
        rootRef.child("users").orderByChild("role").equalTo("patient").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalPatients.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Active Staff Count
        rootRef.child("users").orderByChild("role").equalTo("staff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvActiveStaff.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Appointments Today
        String today = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        rootRef.child("appointments").orderByChild("date").equalTo(today).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvAppointmentsToday.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Queue Efficiency (Logic: % of tickets that are not "Waiting")
        rootRef.child("queue").child("tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long startOfToday = cal.getTimeInMillis();

                long total = 0;
                long active = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Long timestamp = ds.child("timestamp").getValue(Long.class);
                    if (timestamp != null && timestamp >= startOfToday) {
                        total++;
                        String status = ds.child("status").getValue(String.class);
                        if ("Now Serving".equals(status)) {
                            active++;
                        }
                    }
                }

                if (total == 0) {
                    tvQueueEfficiency.setText("100%");
                    return;
                }

                int efficiency = (int) ((active * 100) / total);
                tvQueueEfficiency.setText(efficiency + "%");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchDepartmentOverview() {
        rootRef.child("queue").child("tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (llDepartmentStats == null) return;
                
                // Keep the header and clear the rest
                View header = llDepartmentStats.getChildAt(0);
                llDepartmentStats.removeAllViews();
                llDepartmentStats.addView(header);

                java.util.Map<String, Integer> deptCounts = new java.util.HashMap<>();
                int maxCount = 0;

                // Get start of today to filter old tickets
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long startOfToday = cal.getTimeInMillis();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String specialty = ds.child("specialty").getValue(String.class);
                    String status = ds.child("status").getValue(String.class);
                    Long timestamp = ds.child("timestamp").getValue(Long.class);
                    
                    // Only count active patients (Waiting or Now Serving) from today
                    boolean isToday = timestamp != null && timestamp >= startOfToday;
                    boolean isActive = "Waiting".equals(status) || "Now Serving".equals(status);

                    if (specialty != null && isActive && isToday) {
                        int count = deptCounts.getOrDefault(specialty, 0) + 1;
                        deptCounts.put(specialty, count);
                        if (count > maxCount) maxCount = count;
                    }
                }

                if (deptCounts.isEmpty()) {
                    TextView empty = new TextView(AdminDashboardActivity.this);
                    empty.setText("No active queue data today");
                    empty.setGravity(android.view.Gravity.CENTER);
                    empty.setPadding(0, 50, 0, 50);
                    llDepartmentStats.addView(empty);
                } else {
                    for (java.util.Map.Entry<String, Integer> entry : deptCounts.entrySet()) {
                        addDepartmentBar(entry.getKey(), entry.getValue(), maxCount);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addDepartmentBar(String name, int count, int max) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 8, 0, 8);

        TextView label = new TextView(this);
        label.setText(name + " (" + count + ")");
        label.setTextSize(13);
        label.setTextColor(0xFF333333);
        row.addView(label);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(max);
        bar.setProgress(count);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF3F51B5));
        bar.setScaleY(1.5f);
        row.addView(bar);

        row.setClickable(true);
        row.setFocusable(true);
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setBackgroundResource(outValue.resourceId);

        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientRecordsActivity.class);
            intent.putExtra("filterDept", name);
            startActivity(intent);
        });

        llDepartmentStats.addView(row);
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
                        NotificationHelper.showNotification(AdminDashboardActivity.this, notif.getTitle(), notif.getMessage());
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
