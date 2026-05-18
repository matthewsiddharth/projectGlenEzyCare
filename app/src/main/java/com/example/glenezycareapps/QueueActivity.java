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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueActivity extends AppCompatActivity {

    TextView tvQueueNumber, tvQueueStatus;
    Spinner spinnerSpecialty, spinnerDoctor;
    Button btnGenerateQueue;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;
    DatabaseReference userRef;
    FirebaseAuth mAuth;

    private Map<String, String[]> specialtyDoctorMap;
    private Map<String, String> specialtyFloorMap;

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

        initializeData();
        setupSpinners();

        btnGenerateQueue.setOnClickListener(v -> generateQueue());
    }

    private void initializeData() {
        specialtyDoctorMap = new HashMap<>();
        specialtyDoctorMap.put("Cardiology", new String[]{"Dr Ali (Cardiologist)", "Dr Sarah (Cardiologist)"});
        specialtyDoctorMap.put("ENT (Otorhinolaryngology)", new String[]{"Dr John (ENT Specialist)", "Dr Lim (ENT Specialist)"});
        specialtyDoctorMap.put("Orthopedic Surgery", new String[]{"Dr Siti (Orthopedic Surgeon)", "Dr Wong (Orthopedic Surgeon)"});
        specialtyDoctorMap.put("Dermatology", new String[]{"Dr Tan (Dermatologist)", "Dr Kumar (Dermatologist)"});
        specialtyDoctorMap.put("Pediatrics", new String[]{"Dr Raj (Pediatrician)", "Dr Low (Pediatrician)"});
        specialtyDoctorMap.put("Obstetrics & Gynecology", new String[]{"Dr Ng (Gynecologist)", "Dr Ibrahim (Gynecologist)"});
        specialtyDoctorMap.put("Ophthalmology", new String[]{"Dr Chen (Ophthalmologist)", "Dr Kumar (Ophthalmologist)"});
        specialtyDoctorMap.put("Gastroenterology", new String[]{"Dr Gupta (Gastroenterologist)", "Dr Lopez (Gastroenterologist)"});
        specialtyDoctorMap.put("Neurology", new String[]{"Dr White (Neurologist)", "Dr Black (Neurologist)"});
        specialtyDoctorMap.put("Psychiatry", new String[]{"Dr Green (Psychiatrist)", "Dr Blue (Psychiatrist)"});
        specialtyDoctorMap.put("Dentistry", new String[]{"Dr Smile (Dentist)", "Dr Tooth (Dentist)"});
        specialtyDoctorMap.put("General Surgery", new String[]{"Dr Sharp (Surgeon)", "Dr Cut (Surgeon)"});

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

    private void setupSpinners() {
        List<String> specialties = new ArrayList<>(specialtyDoctorMap.keySet());
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specialties);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSpecialty = specialties.get(position);
                updateDoctorSpinner(selectedSpecialty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDoctorSpinner(String specialty) {
        String[] doctors = specialtyDoctorMap.get(specialty);
        if (doctors != null) {
            ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctors);
            spinnerDoctor.setAdapter(doctorAdapter);
        }
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

        String userId = mAuth.getCurrentUser().getUid();
        String specialty = spinnerSpecialty.getSelectedItem().toString();
        String doctor = spinnerDoctor.getSelectedItem().toString();
        String floor = specialtyFloorMap.get(specialty);

        queueRef.child("nextTicketNumber")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                int nextNumber = 1;

                                if(snapshot.exists()) {
                                    nextNumber = snapshot.getValue(Integer.class);
                                }

                                String queueNumber = "Q" + String.format("%03d", nextNumber);

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
                                }

                                queueRef.child("nextTicketNumber").setValue(nextNumber + 1);

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
                            public void onCancelled(
                                    @NonNull DatabaseError error) {
                                Toast.makeText(QueueActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}