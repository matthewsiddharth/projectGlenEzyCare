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
        adapter = new AppointmentAdapter(appointmentList, true, appointment -> {
            showAppointmentActionDialog(appointment);
        });
        rvAppointments.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("appointments");
        fetchAppointments();
    }

    private void showAppointmentActionDialog(AppointmentModel appointment) {
        String[] actions = {"Mark as Serving", "Mark as Completed", "Cancel Appointment", "Delete Record"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Appointment");
        builder.setItems(actions, (dialog, which) -> {
            if (which == 0) {
                updateStatus(appointment.getAppointmentId(), "Serving");
            } else if (which == 1) {
                updateStatus(appointment.getAppointmentId(), "Completed");
            } else if (which == 2) {
                updateStatus(appointment.getAppointmentId(), "Cancelled");
            } else if (which == 3) {
                deleteAppointment(appointment.getAppointmentId());
            }
        });
        builder.show();
    }

    private void updateStatus(String id, String status) {
        databaseReference.child(id).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show());
    }

    private void deleteAppointment(String id) {
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Appointment deleted", Toast.LENGTH_SHORT).show());
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
