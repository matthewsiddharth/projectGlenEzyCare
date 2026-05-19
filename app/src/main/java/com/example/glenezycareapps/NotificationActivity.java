package com.example.glenezycareapps;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private LinearLayout llEmptyState;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        llEmptyState = findViewById(R.id.llEmptyState);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onAction(NotificationModel notification, String status) {
                handleNotificationAction(notification, status);
            }
        });
        rvNotifications.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        if (mAuth.getCurrentUser() != null) {
            fetchNotifications();
            markAllAsRead();
        }
    }

    private void fetchNotifications() {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("notifications").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationModel notification = ds.getValue(NotificationModel.class);
                    if (notification != null) {
                        notification.setId(ds.getKey());
                        notificationList.add(notification);
                    }
                }
                Collections.reverse(notificationList); // Newest first
                adapter.notifyDataSetChanged();

                if (notificationList.isEmpty()) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    rvNotifications.setVisibility(View.GONE);
                } else {
                    llEmptyState.setVisibility(View.GONE);
                    rvNotifications.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAllAsRead() {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("notifications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().child("read").setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleNotificationAction(NotificationModel notification, String status) {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("notifications").child(uid).child(notification.getId()).child("actionStatus").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Response sent: " + status, Toast.LENGTH_SHORT).show();
                });
        
        // If they decline, we could potentially notify the staff here as well
        if (status.equals("declined")) {
            // Logic to inform staff/admin or mark appointment as "Patient Declined"
            if (notification.getAppointmentId() != null) {
                rootRef.child("appointments").child(notification.getAppointmentId()).child("status").setValue("Patient Declined");
            }
        }
    }
}
