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
import java.util.Map;

public class AppointmentActivity extends AppCompatActivity {

    Spinner spinnerSpecialty, spinnerDoctor, spinnerTime;
    EditText etDate;
    Button btnBookAppointment;

    DatabaseReference appointmentRef, userRef;
    String currentUserId, currentUserName;

    // Map to store specialists for each specialty
    private Map<String, String[]> specialtyDoctorMap;

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

        // Initialize doctor data
        initializeDoctorData();

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

            HashMap<String, String> appointmentMap = new HashMap<>();
            appointmentMap.put("patientId", currentUserId);
            appointmentMap.put("patientName", currentUserName != null ? currentUserName : "Unknown Patient");
            appointmentMap.put("doctor", doctor);
            appointmentMap.put("specialty", specialty);
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

    private void initializeDoctorData() {
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

        String[] timeSlots = {
                "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", 
                "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", 
                "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM"
        };

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeSlots);
        spinnerTime.setAdapter(timeAdapter);
    }

    private void updateDoctorSpinner(String specialty) {
        String[] doctors = specialtyDoctorMap.get(specialty);
        if (doctors != null) {
            ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctors);
            spinnerDoctor.setAdapter(doctorAdapter);
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