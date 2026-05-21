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
import androidx.recyclerview.widget.ItemTouchHelper;
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

public class AppointmentManagementActivity extends AppCompatActivity {
    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private List<AppointmentModel> appointmentList;
    private DatabaseReference databaseReference;
    private String userSpecialty = "";
    private String userRole = "";
    private String selectedFilterDept = "All Departments";
    private Spinner spinnerDeptFilter;
    private View filterContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_management);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        spinnerDeptFilter = findViewById(R.id.spinnerDeptFilter);
        filterContainer = findViewById(R.id.filterContainer);

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList, true, false, new AppointmentAdapter.OnAppointmentClickListener() {
            @Override
            public void onAppointmentClick(AppointmentModel appointment) {
                showAppointmentActionDialog(appointment);
            }

            @Override
            public void onCancelClick(AppointmentModel appointment) {
                // Not used in management view
            }
        });
        rvAppointments.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("appointments");
        setupSwipeToDelete();
        getCurrentUserInfo();
    }

    private void getCurrentUserInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(uid);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.child("role").getValue(String.class);
                    userSpecialty = snapshot.child("specialty").getValue(String.class);
                    if (userRole == null) userRole = "patient";
                    if (userSpecialty == null) userSpecialty = "";
                    
                    if ("admin".equals(userRole)) {
                        setupAdminFilter();
                    }
                    
                    fetchAppointments();
                } else {
                    Toast.makeText(AppointmentManagementActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentManagementActivity.this, "Error fetching user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdminFilter() {
        filterContainer.setVisibility(View.VISIBLE);
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
                selectedFilterDept = depts[position];
                fetchAppointments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                AppointmentModel appointment = appointmentList.get(position);

                if ("Cancelled".equals(appointment.getStatus()) || "Completed".equals(appointment.getStatus())) {
                    new AlertDialog.Builder(AppointmentManagementActivity.this)
                            .setTitle("Delete Record")
                            .setMessage("Are you sure you want to permanently delete this appointment record?")
                            .setPositiveButton("Delete", (d, w) -> {
                                databaseReference.child(appointment.getAppointmentId()).removeValue()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(AppointmentManagementActivity.this, "Record deleted permanently", Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("Cancel", (d, w) -> {
                                adapter.notifyItemChanged(position);
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(AppointmentManagementActivity.this, "Only Cancelled or Completed appointments can be deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvAppointments);
    }

    private void showAppointmentActionDialog(AppointmentModel appointment) {
        String[] actions = {"Mark as Serving", "Mark as Completed", "Cancel Appointment"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Appointment");
        builder.setItems(actions, (dialog, which) -> {
            if (which == 0) {
                updateStatus(appointment.getAppointmentId(), "Serving");
            } else if (which == 1) {
                updateStatus(appointment.getAppointmentId(), "Completed");
            } else if (which == 2) {
                updateStatus(appointment.getAppointmentId(), "Cancelled");
            }
        });
        builder.show();
    }

    private void updateStatus(String id, String status) {
        databaseReference.child(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                AppointmentModel appointment = task.getResult().getValue(AppointmentModel.class);
                if (appointment != null) {
                    databaseReference.child(id).child("status").setValue(status)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                                if (status.equals("Cancelled")) {
                                    sendCancellationNotification(appointment);
                                }
                                
                                // If completed or cancelled, clear the active queue ticket for this patient
                                if (status.equals("Completed") || status.equals("Cancelled")) {
                                    FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                            .getReference("users").child(appointment.getPatientId()).child("currentTicket").removeValue();
                                }
                            });
                }
            }
        });
    }

    private void sendCancellationNotification(AppointmentModel appointment) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("notifications").child(appointment.getPatientId());
        
        String notifId = notifRef.push().getKey();
        if (notifId != null) {
            NotificationModel notification = new NotificationModel(
                    notifId,
                    appointment.getPatientId(),
                    "Appointment Cancelled",
                    "Your appointment for " + appointment.getSpecialty() + " on " + appointment.getDate() + " has been cancelled by the hospital.",
                    System.currentTimeMillis(),
                    "cancellation",
                    appointment.getAppointmentId()
            );
            notifRef.child(notifId).setValue(notification);
        }
    }

    private void fetchAppointments() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentModel appointment = dataSnapshot.getValue(AppointmentModel.class);
                    if (appointment != null) {
                        appointment.setAppointmentId(dataSnapshot.getKey());
                        
                        // Filter: If user is staff, only show appointments for their specialty
                        if ("staff".equals(userRole)) {
                            if (appointment.getSpecialty() != null && appointment.getSpecialty().equals(userSpecialty)) {
                                appointmentList.add(appointment);
                            }
                        } else if ("admin".equals(userRole)) {
                            if ("All Departments".equals(selectedFilterDept) || 
                                (appointment.getSpecialty() != null && appointment.getSpecialty().equals(selectedFilterDept))) {
                                appointmentList.add(appointment);
                            }
                        } else {
                            // Others see everything
                            appointmentList.add(appointment);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentManagementActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
