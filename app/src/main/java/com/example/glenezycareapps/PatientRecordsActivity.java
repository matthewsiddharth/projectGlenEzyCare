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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class PatientRecordsActivity extends AppCompatActivity {
    private RecyclerView rvPatientRecords;
    private PatientAdapter adapter;
    private List<UserModel> patientList;
    private DatabaseReference databaseReference;
    private String filterDept = "All Departments";
    private final java.util.Set<String> patientIdsInDept = new java.util.HashSet<>();
    private Spinner spinnerDeptFilter;
    private View filterContainer;
    private String userRole = "";
    private String userSpecialty = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_records);

        filterDept = getIntent().getStringExtra("filterDept");
        if (filterDept == null) filterDept = "All Departments";

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvPatientRecords = findViewById(R.id.rvPatientRecords);
        rvPatientRecords.setLayoutManager(new LinearLayoutManager(this));

        spinnerDeptFilter = findViewById(R.id.spinnerDeptFilter);
        filterContainer = findViewById(R.id.filterContainer);

        patientList = new ArrayList<>();
        adapter = new PatientAdapter(patientList, patient -> {
            showPatientDialog(patient);
        });
        rvPatientRecords.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        
        checkUserRoleAndSetup();
    }

    private void checkUserRoleAndSetup() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.child("role").getValue(String.class);
                    userSpecialty = snapshot.child("specialty").getValue(String.class);
                    
                    if ("staff".equals(userRole)) {
                        // Staff can only see their own department, hide filter
                        filterContainer.setVisibility(View.GONE);
                        filterDept = (userSpecialty != null && !userSpecialty.isEmpty()) ? userSpecialty : "All Departments";
                    } else {
                        // Admin can see all and filter
                        filterContainer.setVisibility(View.VISIBLE);
                        setupFilterSpinner();
                    }

                    if (!"All Departments".equals(filterDept)) {
                        fetchPatientsInDept(filterDept);
                    } else {
                        fetchPatients();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchPatients();
            }
        });
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

        // Set initial selection based on intent
        for (int i = 0; i < depts.length; i++) {
            if (depts[i].equals(filterDept)) {
                spinnerDeptFilter.setSelection(i);
                break;
            }
        }

        spinnerDeptFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = depts[position];
                if (!selected.equals(filterDept)) {
                    filterDept = selected;
                    if (!"All Departments".equals(filterDept)) {
                        fetchPatientsInDept(filterDept);
                    } else {
                        fetchPatients();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchPatientsInDept(String dept) {
        DatabaseReference ticketsRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("queue/tickets");
        
        ticketsRef.orderByChild("specialty").equalTo(dept).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientIdsInDept.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = ds.child("userId").getValue(String.class);
                    if (uid != null) patientIdsInDept.add(uid);
                }
                fetchPatients();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showPatientDialog(UserModel patient) {
        String[] options = {"View Details", "Delete Record", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(patient.getFullName());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                String details = "Email: " + patient.getEmail() + "\nPhone: " + (patient.getPhone() != null ? patient.getPhone() : "Not provided");
                Toast.makeText(this, details, Toast.LENGTH_LONG).show();
            } else if (which == 1) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Record")
                        .setMessage("Are you sure you want to delete this patient?")
                        .setPositiveButton("Yes", (d, w) -> {
                            databaseReference.child(patient.getUserId()).removeValue();
                            Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        builder.show();
    }

    private void fetchPatients() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null && "patient".equals(user.getRole())) {
                        user.setUserId(dataSnapshot.getKey());
                        
                        if ("All Departments".equals(filterDept)) {
                            patientList.add(user);
                        } else if (patientIdsInDept.contains(user.getUserId())) {
                            patientList.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PatientRecordsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
