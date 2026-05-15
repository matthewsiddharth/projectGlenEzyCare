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

public class PatientRecordsActivity extends AppCompatActivity {
    private RecyclerView rvPatientRecords;
    private PatientAdapter adapter;
    private List<UserModel> patientList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_records);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvPatientRecords = findViewById(R.id.rvPatientRecords);
        rvPatientRecords.setLayoutManager(new LinearLayoutManager(this));

        patientList = new ArrayList<>();
        adapter = new PatientAdapter(patientList, patient -> {
            showPatientDialog(patient);
        });
        rvPatientRecords.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        fetchPatients();
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
                        patientList.add(user);
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
