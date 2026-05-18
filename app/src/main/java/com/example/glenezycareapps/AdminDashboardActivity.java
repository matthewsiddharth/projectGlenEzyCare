package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    View btnManageStaff, btnManagePatients, btnQueueControl, btnViewQueueStatus;
    LinearLayout btnLogout;
    TextView tvAdminName, tvTotalPatients, tvActiveStaff, tvAppointmentsToday;
    ImageView ivAdminProfile, btnMenu;
    DrawerLayout drawerLayout;

    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnManageStaff = findViewById(R.id.btnManageStaff);
        btnManagePatients = findViewById(R.id.btnManagePatients);
        btnQueueControl = findViewById(R.id.btnQueueControl);
        btnViewQueueStatus = findViewById(R.id.btnViewQueueStatus);
        btnLogout = findViewById(R.id.btnLogout);
        tvAdminName = findViewById(R.id.tvAdminName);
        ivAdminProfile = findViewById(R.id.ivAdminProfile);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        
        tvTotalPatients = findViewById(R.id.tvTotalPatientsCount);
        tvActiveStaff = findViewById(R.id.tvActiveStaffCount);
        tvAppointmentsToday = findViewById(R.id.tvAppointmentsTodayCount);

        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        fetchAdminData();
        fetchRealTimeStats();

        btnManageStaff.setOnClickListener(v -> startActivity(new Intent(this, StaffManagementActivity.class)));
        btnManagePatients.setOnClickListener(v -> startActivity(new Intent(this, PatientRecordsActivity.class)));
        btnQueueControl.setOnClickListener(v -> startActivity(new Intent(this, QueueCallingActivity.class)));
        btnViewQueueStatus.setOnClickListener(v -> startActivity(new Intent(this, QueueStatusActivity.class)));

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ivAdminProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

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
        // Patients Count
        rootRef.child("users").orderByChild("role").equalTo("patient").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (tvTotalPatients != null) tvTotalPatients.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Staff Count
        rootRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int staffCount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String role = ds.child("role").getValue(String.class);
                    if (role != null && (role.equals("staff") || role.equals("admin"))) staffCount++;
                }
                if (tvActiveStaff != null) tvActiveStaff.setText(String.valueOf(staffCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Appointments Today
        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
        rootRef.child("appointments").orderByChild("date").equalTo(today).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (tvAppointmentsToday != null) tvAppointmentsToday.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}