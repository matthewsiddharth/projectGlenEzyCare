package com.example.glenezycareapps;

import android.os.Bundle;
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
        adapter = new AppointmentAdapter(appointmentList, false, true, new AppointmentAdapter.OnAppointmentClickListener() {
            @Override
            public void onAppointmentClick(AppointmentModel appointment) {
                // Potential details view
            }

            @Override
            public void onCancelClick(AppointmentModel appointment) {
                new AlertDialog.Builder(AppointmentHistoryActivity.this)
                        .setTitle("Cancel Appointment")
                        .setMessage("Do you want to cancel this appointment?")
                        .setPositiveButton("Yes", (d, w) -> {
                            databaseReference.child(appointment.getAppointmentId()).child("status").setValue("Cancelled");
                            Toast.makeText(AppointmentHistoryActivity.this, "Appointment Cancelled", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        rvAppointmentHistory.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("appointments");
        
        setupSwipeToDelete();
        fetchHistory();
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
                    databaseReference.child(appointment.getAppointmentId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(AppointmentHistoryActivity.this, "Record removed from history", Toast.LENGTH_SHORT).show());
                } else {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(AppointmentHistoryActivity.this, "Only Cancelled or Completed appointments can be removed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvAppointmentHistory);
    }

    private void fetchHistory() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentModel appointment = dataSnapshot.getValue(AppointmentModel.class);
                    if (appointment != null && currentUserId.equals(appointment.getPatientId())) {
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
