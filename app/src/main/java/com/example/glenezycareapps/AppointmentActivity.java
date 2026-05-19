// ==============================
// AppointmentActivity.java
// ==============================

package com.example.glenezycareapps;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
        appointmentRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("appointments");
        userRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users").child(currentUserId);

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
            if (spinnerSpecialty.getSelectedItem() == null || spinnerDoctor.getSelectedItem() == null || spinnerTime.getSelectedItem() == null) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String specialty = spinnerSpecialty.getSelectedItem().toString();
            String doctor = spinnerDoctor.getSelectedItem().toString();
            String time = spinnerTime.getSelectedItem().toString();

            if (date.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (doctor.equals("No doctors available")) {
                Toast.makeText(this, "Cannot book: No doctor available for this specialty", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> appointmentMap = new HashMap<>();
            appointmentMap.put("patientId", currentUserId);
            appointmentMap.put("patientName", currentUserName != null ? currentUserName : "Unknown Patient");
            appointmentMap.put("doctor", doctor);
            appointmentMap.put("specialty", specialty);
            appointmentMap.put("date", date);
            appointmentMap.put("time", time);
            appointmentMap.put("status", "Pending");

            String pushId = appointmentRef.push().getKey();
            if (pushId != null) {
                appointmentRef.child(pushId).setValue(appointmentMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Appointment Booked", Toast.LENGTH_SHORT).show();
                        sendBookingNotification(pushId, specialty, doctor, date, time);
                        notifyStaffOfNewAppointment(pushId, specialty, doctor, date, time);
                        finish();
                    } else {
                        Toast.makeText(this, "Booking failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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

        String[] timeSlots = {
                "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", 
                "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", 
                "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM"
        };

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeSlots);
        spinnerTime.setAdapter(timeAdapter);
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

                ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(AppointmentActivity.this, android.R.layout.simple_spinner_dropdown_item, doctors);
                spinnerDoctor.setAdapter(doctorAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentActivity.this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyStaffOfNewAppointment(String appointmentId, String specialty, String doctor, String date, String time) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");
        
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        "New Appointment Booking",
                                        (currentUserName != null ? currentUserName : "A patient") + " booked for " + specialty + " with " + doctor + " on " + date + " at " + time + ".",
                                        System.currentTimeMillis(),
                                        "new_appointment",
                                        appointmentId
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

    private void sendBookingNotification(String appointmentId, String specialty, String doctor, String date, String time) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("notifications").child(currentUserId);
        
        String notifId = notifRef.push().getKey();
        if (notifId != null) {
            NotificationModel notification = new NotificationModel(
                    notifId,
                    currentUserId,
                    "Appointment Confirmed",
                    "Your appointment for " + specialty + " with " + doctor + " on " + date + " at " + time + " is confirmed.",
                    System.currentTimeMillis(),
                    "booking",
                    appointmentId
            );
            notifRef.child(notifId).setValue(notification);
        }
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