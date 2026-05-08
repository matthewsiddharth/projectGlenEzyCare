// ==============================
// AppointmentActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class AppointmentActivity extends AppCompatActivity {

    Spinner spinnerSpecialty, spinnerDoctor, spinnerTime;
    EditText etDate;
    Button btnBookAppointment;

    DatabaseReference appointmentRef, userRef;
    String currentUserId, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        spinnerTime = findViewById(R.id.spinnerTime);
        etDate = findViewById(R.id.etDate);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentRef = FirebaseDatabase.getInstance().getReference("appointments");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Fetch current user's name
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("fullName").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        setupSpinners();
        setupDatePicker();

        btnBookAppointment.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = spinnerTime.getSelectedItem().toString();

            if (date.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> appointmentMap = new HashMap<>();
            appointmentMap.put("patientId", currentUserId);
            appointmentMap.put("patientName", currentUserName != null ? currentUserName : "Unknown Patient");
            appointmentMap.put("doctor", spinnerDoctor.getSelectedItem().toString());
            appointmentMap.put("specialty", spinnerSpecialty.getSelectedItem().toString());
            appointmentMap.put("date", date);
            appointmentMap.put("time", time);
            appointmentMap.put("status", "Pending");

            appointmentRef.push().setValue(appointmentMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Appointment Booked", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Booking failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        String[] specialties = {"Cardiology", "ENT", "Orthopedic"};
        String[] doctors = {"Dr Ali", "Dr Sarah", "Dr John"};
        String[] timeSlots = {
                "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", 
                "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", 
                "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM"
        };

        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specialties);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctors);
        spinnerDoctor.setAdapter(doctorAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeSlots);
        spinnerTime.setAdapter(timeAdapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        etDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }
}