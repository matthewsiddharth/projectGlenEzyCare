package com.example.glenezycareapps;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppointmentActivity extends AppCompatActivity {

    Spinner spinnerSpecialty, spinnerDoctor;
    EditText etDate, etTime;
    Button btnBookAppointment;

    private Map<String, List<String>> specialtyDoctorMap;
    private ArrayAdapter<String> doctorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        spinnerSpecialty = findViewById(R.id.spinnerSpecialty);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);

        setupSpecialtiesAndDoctors();
        setupDateTimePickers();

        btnBookAppointment.setOnClickListener(v -> {
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Appointment Booked Successfully for " + date + " at " + time, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpecialtiesAndDoctors() {
        specialtyDoctorMap = new HashMap<>();

        List<String> cardiologyDoctors = new ArrayList<>();
        cardiologyDoctors.add("Dr. Ali (Cardiologist)");
        cardiologyDoctors.add("Dr. Sarah (Cardiologist)");
        specialtyDoctorMap.put("Cardiology", cardiologyDoctors);

        List<String> orthopedicDoctors = new ArrayList<>();
        orthopedicDoctors.add("Dr. John (Orthopedic)");
        orthopedicDoctors.add("Dr. Mike (Orthopedic)");
        specialtyDoctorMap.put("Orthopedic", orthopedicDoctors);

        List<String> entDoctors = new ArrayList<>();
        entDoctors.add("Dr. Jane (ENT)");
        specialtyDoctorMap.put("ENT", entDoctors);

        List<String> neurologyDoctors = new ArrayList<>();
        neurologyDoctors.add("Dr. Emily (Neurology)");
        specialtyDoctorMap.put("Neurology", neurologyDoctors);

        String[] specialties = specialtyDoctorMap.keySet().toArray(new String[0]);

        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, specialties);
        spinnerSpecialty.setAdapter(specialtyAdapter);

        doctorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerDoctor.setAdapter(doctorAdapter);

        spinnerSpecialty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSpecialty = specialties[position];
                updateDoctorSpinner(selectedSpecialty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateDoctorSpinner(String specialty) {
        List<String> doctors = specialtyDoctorMap.get(specialty);
        doctorAdapter.clear();
        if (doctors != null) {
            doctorAdapter.addAll(doctors);
        }
        doctorAdapter.notifyDataSetChanged();
    }

    private void setupDateTimePickers() {
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> etDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                    year, month, day);
            datePickerDialog.show();
        });

        etTime.setFocusable(false);
        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute1) -> etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true);
            timePickerDialog.show();
        });
    }
}