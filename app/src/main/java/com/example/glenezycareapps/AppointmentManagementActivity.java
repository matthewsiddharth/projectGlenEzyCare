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

public class AppointmentManagementActivity extends AppCompatActivity {
    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private List<AppointmentModel> appointmentList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_management);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

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
        fetchAppointments();
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
                        appointmentList.add(appointment);
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
