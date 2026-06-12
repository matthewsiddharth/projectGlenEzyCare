// ==============================
// QueueActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QueueActivity extends AppCompatActivity {

    TextView tvQueueNumber, tvQueueStatus;
    Spinner spinnerSpecialty, spinnerDoctor;
    Button btnGenerateQueue;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;
    DatabaseReference userRef;
    FirebaseAuth mAuth;

    private Map<String, String> specialtyFloorMap;
    private Map<String, String> specialtyPrefixMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        tvQueueStatus = findViewById(R.id.tvQueueStatus);
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnGenerateQueue = findViewById(R.id.btnGenerateQueue);

        mAuth = FirebaseAuth.getInstance();
        queueRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("queue");
        
        if (mAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(mAuth.getCurrentUser().getUid());
        }

        initializeFloorData();
        initializePrefixData();
        setupSpinners();

        btnGenerateQueue.setOnClickListener(v -> generateQueue());
    }

    private void initializeFloorData() {
        specialtyFloorMap = new HashMap<>();
        specialtyFloorMap.put("Cardiology", "Level 2, Gleneagles Hospital");
        specialtyFloorMap.put("ENT (Otorhinolaryngology)", "Level 3, Gleneagles Hospital");
        specialtyFloorMap.put("Orthopedic Surgery", "Level 4, Gleneagles Hospital");
        specialtyFloorMap.put("Dermatology", "Level 2, Gleneagles Hospital");
        specialtyFloorMap.put("Pediatrics", "Level 5, Gleneagles Hospital");
        specialtyFloorMap.put("Obstetrics & Gynecology", "Level 5, Gleneagles Hospital");
        specialtyFloorMap.put("Ophthalmology", "Level 3, Gleneagles Hospital");
        specialtyFloorMap.put("Gastroenterology", "Level 4, Gleneagles Hospital");
        specialtyFloorMap.put("Neurology", "Level 6, Gleneagles Hospital");
        specialtyFloorMap.put("Psychiatry", "Level 6, Gleneagles Hospital");
        specialtyFloorMap.put("Dentistry", "Level 3, Gleneagles Hospital");
        specialtyFloorMap.put("General Surgery", "Level 4, Gleneagles Hospital");
    }

    private void initializePrefixData() {
        specialtyPrefixMap = new HashMap<>();
        specialtyPrefixMap.put("Cardiology", "CAR");
        specialtyPrefixMap.put("ENT (Otorhinolaryngology)", "ENT");
        specialtyPrefixMap.put("Orthopedic Surgery", "ORT");
        specialtyPrefixMap.put("Dermatology", "DER");
        specialtyPrefixMap.put("Pediatrics", "PED");
        specialtyPrefixMap.put("Obstetrics & Gynecology", "OBS");
        specialtyPrefixMap.put("Ophthalmology", "OPH");
        specialtyPrefixMap.put("Gastroenterology", "GAS");
        specialtyPrefixMap.put("Neurology", "NEU");
        specialtyPrefixMap.put("Psychiatry", "PSY");
        specialtyPrefixMap.put("Dentistry", "DEN");
        specialtyPrefixMap.put("General Surgery", "GEN");
    }

    private void setupSpinners() {
        String[] specialties = {
                "Cardiology", "ENT (Otorhinolaryngology)", "Orthopedic Surgery",
                "Dermatology", "Pediatrics", "Obstetrics & Gynecology",
                "Ophthalmology", "Gastroenterology", "Neurology",
                "Psychiatry", "Dentistry", "General Surgery"
        };
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specialties);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSpecialty = specialties[position];
                updateDoctorSpinner(selectedSpecialty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDoctorSpinner(String specialty) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        usersRef.orderByChild("specialty").equalTo(specialty).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> doctors = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String role = ds.child("role").getValue(String.class);
                        if (role != null && (role.contains("staff") || role.contains("admin"))) {
                            String doctorName = ds.child("fullName").getValue(String.class);
                            if (doctorName != null) {
                                doctors.add(doctorName);
                            }
                        }
                    }
                }

                if (doctors.isEmpty()) {
                    doctors.add("No doctors available");
                }

                ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(QueueActivity.this, android.R.layout.simple_spinner_dropdown_item, doctors);
                spinnerDoctor.setAdapter(doctorAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QueueActivity.this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateQueue() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to generate a ticket", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerSpecialty.getSelectedItem() == null || spinnerDoctor.getSelectedItem() == null) {
            Toast.makeText(this, "Please select specialty and doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctor = spinnerDoctor.getSelectedItem().toString();
        if (doctor.equals("No doctors available")) {
            Toast.makeText(this, "Cannot generate ticket: No doctor available", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String specialty = spinnerSpecialty.getSelectedItem().toString();
        String floor = specialtyFloorMap.get(specialty);
        String prefix = specialtyPrefixMap.getOrDefault(specialty, "Q");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Use specialty-specific counter with daily reset logic
        queueRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int nextNumber = 1;
                String lastResetDate = "";

                if (snapshot.child("lastResetDate").child(specialty).exists()) {
                    lastResetDate = snapshot.child("lastResetDate").child(specialty).getValue(String.class);
                }

                if (todayDate.equals(lastResetDate)) {
                    // Same day, use existing counter
                    if (snapshot.child("nextTicketNumber").child(specialty).exists()) {
                        Integer val = snapshot.child("nextTicketNumber").child(specialty).getValue(Integer.class);
                        if (val != null) nextNumber = val;
                    }
                } else {
                    // New day! Reset counter to 1
                    nextNumber = 1;
                    queueRef.child("lastResetDate").child(specialty).setValue(todayDate);
                    // Also reset nowServing for this specialty for the new day
                    queueRef.child("nowServing").child(specialty).setValue(0);
                }

                String queueNumber = prefix + String.format("%03d", nextNumber);

                HashMap<String, Object> queueMap = new HashMap<>();
                queueMap.put("queueNumber", queueNumber);
                queueMap.put("status", "Waiting");
                queueMap.put("userId", userId);
                queueMap.put("specialty", specialty);
                queueMap.put("doctor", doctor);
                queueMap.put("floor", floor);
                queueMap.put("timestamp", ServerValue.TIMESTAMP);

                // Push to global tickets
                String ticketId = queueRef.child("tickets").push().getKey();
                if (ticketId != null) {
                    queueRef.child("tickets").child(ticketId).setValue(queueMap);

                    // Also save a reference to the patient's own profile
                    userRef.child("currentTicket").setValue(queueMap);

                    // Notify staff and admins
                    notifyStaffOfNewQueue(queueNumber, specialty, doctor);
                }

                queueRef.child("nextTicketNumber").child(specialty).setValue(nextNumber + 1);

                Toast.makeText(
                        QueueActivity.this,
                        "Queue Ticket Generated: " + queueNumber,
                        Toast.LENGTH_SHORT).show();

                // Automatically navigate to Status screen so they can track it
                android.content.Intent intent = new android.content.Intent(QueueActivity.this, QueueStatusActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QueueActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyStaffOfNewQueue(String queueNumber, String specialty, String doctor) {
        DatabaseReference allUsersRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        allUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if (role != null && (role.contains("staff") || role.contains("admin"))) {
                        String staffId = userSnapshot.getKey();
                        if (staffId != null) {
                            DatabaseReference notifRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                    .getReference("notifications").child(staffId);
                            String notifId = notifRef.push().getKey();
                            if (notifId != null) {
                                NotificationModel notification = new NotificationModel(
                                        notifId,
                                        staffId,
                                        "New Queue Ticket Generated",
                                        "Ticket " + queueNumber + " generated for " + specialty + " (" + doctor + ").",
                                        System.currentTimeMillis(),
                                        "new_queue",
                                        null
                                );
                                notifRef.child(notifId).setValue(notification);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}