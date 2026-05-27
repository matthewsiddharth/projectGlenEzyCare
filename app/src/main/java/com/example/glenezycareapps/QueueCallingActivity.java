// ==============================
// QueueCallingActivity.java
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

import com.google.firebase.database.*;

import java.util.Map;

public class QueueCallingActivity extends AppCompatActivity {

    TextView tvCurrentQueue, tvCallingDoctor, tvCallingSpecialty;
    Button btnCallNext, btnCompleteQueue;
    Spinner spinnerAdminDept;
    android.widget.ImageView btnBack;

    DatabaseReference queueRef;
    ValueEventListener nowServingListener;
    private String selectedSpecialty = null;
    private String specialtyPrefix = "Q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_calling);

        selectedSpecialty = getIntent().getStringExtra("specialty");

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);
        tvCallingDoctor = findViewById(R.id.tvCallingDoctor);
        tvCallingSpecialty = findViewById(R.id.tvCallingSpecialty);
        spinnerAdminDept = findViewById(R.id.spinnerAdminDept);

        btnCallNext = findViewById(R.id.btnCallNext);
        btnCompleteQueue = findViewById(R.id.btnCompleteQueue);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        queueRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("queue");

        if (selectedSpecialty != null) {
            specialtyPrefix = getPrefixForSpecialty(selectedSpecialty);
            tvCallingSpecialty.setText("Dept: " + selectedSpecialty);
            loadCurrentQueueStatus();
        } else {
            tvCallingSpecialty.setText("Select Dept to Manage:");
            setupAdminDeptSelector();
        }

        btnCallNext.setOnClickListener(v -> callNextQueue());
        
        btnCompleteQueue.setOnClickListener(v -> completeCurrentServing());
    }

    private void setupAdminDeptSelector() {
        spinnerAdminDept.setVisibility(View.VISIBLE);
        String[] depts = {
                "Select Department", "Cardiology", "ENT (Otorhinolaryngology)",
                "Orthopedic Surgery", "Dermatology", "Pediatrics",
                "Obstetrics & Gynecology", "Ophthalmology", "Gastroenterology",
                "Neurology", "Psychiatry", "Dentistry", "General Surgery"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, depts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdminDept.setAdapter(adapter);

        spinnerAdminDept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedSpecialty = depts[position];
                    specialtyPrefix = getPrefixForSpecialty(selectedSpecialty);
                    loadCurrentQueueStatus();
                } else {
                    selectedSpecialty = null;
                    tvCurrentQueue.setText("Q000");
                    tvCallingDoctor.setText("Select a department");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String getPrefixForSpecialty(String specialty) {
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

    private void loadCurrentQueueStatus() {
        if (selectedSpecialty == null) return;

        if (nowServingListener != null) {
            queueRef.child("nowServing").child(selectedSpecialty).removeEventListener(nowServingListener);
        }

        nowServingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer current = snapshot.getValue(Integer.class);
                if (current != null && current > 0) {
                    String queueNum = specialtyPrefix + String.format("%03d", current);
                    tvCurrentQueue.setText(queueNum);
                    fetchTicketDetails(queueNum);
                } else {
                    tvCurrentQueue.setText(specialtyPrefix + "000");
                    tvCallingDoctor.setText("No one being served");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        queueRef.child("nowServing").child(selectedSpecialty).addValueEventListener(nowServingListener);
    }

    private void fetchTicketDetails(String queueNum) {
        queueRef.child("tickets").orderByChild("queueNumber").equalTo(queueNum)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String doctor = child.child("doctor").getValue(String.class);
                                String specialty = child.child("specialty").getValue(String.class);
                                
                                tvCallingDoctor.setText("Serving for: " + (doctor != null ? doctor : "---"));
                                if (selectedSpecialty == null) {
                                    tvCallingSpecialty.setText("Dept: " + (specialty != null ? specialty : "---"));
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void callNextQueue() {
        if (selectedSpecialty == null) {
            Toast.makeText(this, "Admin must select a specialty to call next", Toast.LENGTH_SHORT).show();
            // In a real app, I'd show a dialog to pick specialty. For now, just toast.
            return;
        }

        queueRef.child("nowServing").child(selectedSpecialty).get().addOnCompleteListener(task -> {
            int currentServing = 0;
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer val = task.getResult().getValue(Integer.class);
                if (val != null) currentServing = val;
            }
            
            final int nextToServe = currentServing + 1;
            
            queueRef.child("nextTicketNumber").child(selectedSpecialty).get().addOnCompleteListener(nextTask -> {
                if (nextTask.isSuccessful()) {
                    Integer nextAvailable = nextTask.getResult().getValue(Integer.class);
                    if (nextAvailable != null && nextToServe < nextAvailable) {
                        // Update specialty nowServing
                        queueRef.child("nowServing").child(selectedSpecialty).setValue(nextToServe);
                        
                        // Find the user for this ticket and update their status to "Serving"
                        String queueNumStr = specialtyPrefix + String.format("%03d", nextToServe);
                        queueRef.child("tickets").orderByChild("queueNumber").equalTo(queueNumStr)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            // Update status in global tickets list
                                            ds.getRef().child("status").setValue("Now Serving");

                                            String userId = ds.child("userId").getValue(String.class);
                                            if (userId != null) {
                                                FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                                        .getReference("users").child(userId).child("currentTicket").child("status").setValue("Now Serving");
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });

                        Toast.makeText(this, "Calling: " + queueNumStr, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No more patients in " + selectedSpecialty + " queue", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void completeCurrentServing() {
        if (selectedSpecialty == null) return;

        queueRef.child("nowServing").child(selectedSpecialty).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer current = task.getResult().getValue(Integer.class);
                if (current != null && current > 0) {
                    String queueNumStr = specialtyPrefix + String.format("%03d", current);
                    
                    // 1. Find the user and remove their ticket from their profile
                    queueRef.child("tickets").orderByChild("queueNumber").equalTo(queueNumStr)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String userId = ds.child("userId").getValue(String.class);
                                        if (userId != null) {
                                            DatabaseReference userRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                                    .getReference("users").child(userId);
                                            
                                            // Move current ticket to history before removing it
                                            userRef.child("currentTicket").get().addOnCompleteListener(ticketTask -> {
                                                if (ticketTask.isSuccessful() && ticketTask.getResult().exists()) {
                                                    DataSnapshot ticketSnap = ticketTask.getResult();
                                                    String historyId = userRef.child("queueHistory").push().getKey();
                                                    if (historyId != null) {
                                                        Map<String, Object> ticketData = (Map<String, Object>) ticketSnap.getValue();
                                                        if (ticketData != null) {
                                                            ticketData.put("completedAt", ServerValue.TIMESTAMP);
                                                            ticketData.put("status", "Completed");
                                                            userRef.child("queueHistory").child(historyId).setValue(ticketData);
                                                        }
                                                    }
                                                }
                                                userRef.child("currentTicket").removeValue();
                                            });
                                        }
                                        // 2. Remove from global tickets list
                                        ds.getRef().removeValue();
                                    }
                                    Toast.makeText(QueueCallingActivity.this, "Patient " + queueNumStr + " marked as Completed.", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }
        });
    }
}