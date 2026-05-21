package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AdminListActivity extends AppCompatActivity {
    private RecyclerView rvAdminList;
    private StaffAdapter adapter;
    private List<UserModel> adminList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvAdminList = findViewById(R.id.rvAdminList);
        rvAdminList.setLayoutManager(new LinearLayoutManager(this));

        adminList = new ArrayList<>();
        // Reusing StaffAdapter as it displays the same user info
        adapter = new StaffAdapter(adminList, new StaffAdapter.OnStaffClickListener() {
            @Override
            public void onStaffClick(UserModel staff) {
                showAdminDialog(staff);
            }

            @Override
            public void onDeleteClick(UserModel staff) {
                confirmDeleteAdmin(staff);
            }
        });
        rvAdminList.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        
        fetchAdmins();
    }

    private void confirmDeleteAdmin(UserModel admin) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Admin Account")
                .setMessage("Are you sure you want to permanently delete " + admin.getFullName() + "'s admin account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseReference.child(admin.getUserId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(AdminListActivity.this, "Admin account removed successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(AdminListActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showAdminDialog(UserModel admin) {
        String details = "Full Name: " + admin.getFullName() +
                "\nEmail: " + admin.getEmail() +
                "\nRole: " + admin.getRole().toUpperCase();

        new AlertDialog.Builder(this)
                .setTitle("Admin Details")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }

    private void fetchAdmins() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null && "admin".equals(user.getRole())) {
                        user.setUserId(dataSnapshot.getKey());
                        adminList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}