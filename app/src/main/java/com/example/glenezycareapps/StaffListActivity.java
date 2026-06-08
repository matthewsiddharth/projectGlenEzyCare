package com.example.glenezycareapps;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

public class StaffListActivity extends AppCompatActivity {
    private RecyclerView rvStaffList;
    private StaffAdapter adapter;
    private List<UserModel> staffList;
    private DatabaseReference databaseReference;
    private Spinner spinnerDeptFilter;
    private String selectedDept = "All Departments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_list);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvStaffList = findViewById(R.id.rvStaffList);
        rvStaffList.setLayoutManager(new LinearLayoutManager(this));

        spinnerDeptFilter = findViewById(R.id.spinnerDeptFilter);
        setupFilterSpinner();

        staffList = new ArrayList<>();
        adapter = new StaffAdapter(staffList, new StaffAdapter.OnStaffClickListener() {
            @Override
            public void onStaffClick(UserModel staff) {
                showStaffDialog(staff);
            }

            @Override
            public void onEditClick(UserModel staff) {
                showEditStaffDialog(staff);
            }

            @Override
            public void onDeleteClick(UserModel staff) {
                confirmDeleteStaff(staff);
            }
        });
        rvStaffList.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        
        fetchStaff();
    }

    private void setupFilterSpinner() {
        String[] depts = {
                "All Departments", "Cardiology", "ENT (Otorhinolaryngology)",
                "Orthopedic Surgery", "Dermatology", "Pediatrics",
                "Obstetrics & Gynecology", "Ophthalmology", "Gastroenterology",
                "Neurology", "Psychiatry", "Dentistry", "General Surgery"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, depts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeptFilter.setAdapter(adapter);

        spinnerDeptFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDept = depts[position];
                fetchStaff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showEditStaffDialog(UserModel staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Staff Info");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Full Name");
        etName.setText(staff.getFullName());
        layout.addView(etName);

        final android.widget.Spinner spSpecialty = new android.widget.Spinner(this);
        String[] depts = {
                "None/Admin", "Cardiology", "ENT (Otorhinolaryngology)",
                "Orthopedic Surgery", "Dermatology", "Pediatrics",
                "Obstetrics & Gynecology", "Ophthalmology", "Gastroenterology",
                "Neurology", "Psychiatry", "Dentistry", "General Surgery"
        };
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, depts);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpecialty.setAdapter(spAdapter);
        
        // Set current specialty
        if (staff.getSpecialty() != null && !staff.getSpecialty().isEmpty()) {
            for (int i = 0; i < depts.length; i++) {
                if (depts[i].equals(staff.getSpecialty())) {
                    spSpecialty.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spSpecialty);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newSpecialty = spSpecialty.getSelectedItem().toString();
            if (newSpecialty.equals("None/Admin")) newSpecialty = "";

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Apply Dr. prefix if it's staff role and missing
            if ("staff".equals(staff.getRole()) && !newName.toLowerCase().startsWith("dr. ")) {
                newName = "Dr. " + newName;
            }

            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("fullName", newName);
            updates.put("specialty", newSpecialty);

            databaseReference.child(staff.getUserId()).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(StaffListActivity.this, "Staff info updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(StaffListActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void confirmDeleteStaff(UserModel staff) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Staff Account")
                .setMessage("Are you sure you want to permanently delete " + staff.getFullName() + "'s staff account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseReference.child(staff.getUserId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(StaffListActivity.this, "Staff account removed successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(StaffListActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showStaffDialog(UserModel staff) {
        String details = "Full Name: " + staff.getFullName() +
                "\nEmail: " + staff.getEmail() +
                "\nRole: " + staff.getRole().toUpperCase() +
                (staff.getSpecialty() != null && !staff.getSpecialty().isEmpty() ? "\nSpecialty: " + staff.getSpecialty() : "");

        new AlertDialog.Builder(this)
                .setTitle("Staff Details")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }

    private void fetchStaff() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                staffList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null && "staff".equals(user.getRole())) {
                        user.setUserId(dataSnapshot.getKey());
                        
                        if ("All Departments".equals(selectedDept) || 
                            (user.getSpecialty() != null && user.getSpecialty().equals(selectedDept))) {
                            staffList.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StaffListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
