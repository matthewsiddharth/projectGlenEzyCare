package com.example.glenezycareapps;

import android.os.Bundle;
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

public class AppointmentHistoryActivity extends AppCompatActivity {
    private RecyclerView rvAppointmentHistory;
    private AppointmentAdapter adapter;
    private List<AppointmentModel> appointmentList;
    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvAppointmentHistory = findViewById(R.id.rvAppointmentHistory);
        rvAppointmentHistory.setLayoutManager(new LinearLayoutManager(this));

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList, false, appointment -> {
            // Patients can see details or cancel pending appointments
            if (appointment.getStatus().equals("Pending")) {
                new AlertDialog.Builder(this)
                        .setTitle("Cancel Appointment")
                        .setMessage("Do you want to cancel this appointment?")
                        .setPositiveButton("Yes", (d, w) -> {
                            databaseReference.child(appointment.getAppointmentId()).child("status").setValue("Cancelled");
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        rvAppointmentHistory.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments");
        fetchHistory();
    }

    private void fetchHistory() {
        databaseReference.orderByChild("patientId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(AppointmentHistoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
