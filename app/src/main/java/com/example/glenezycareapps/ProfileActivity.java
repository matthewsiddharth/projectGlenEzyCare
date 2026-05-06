package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone;
    Button btnUpdateProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        btnUpdateProfile.setOnClickListener(v ->
                Toast.makeText(this,
                        "Profile Updated",
                        Toast.LENGTH_SHORT).show());
    }
}