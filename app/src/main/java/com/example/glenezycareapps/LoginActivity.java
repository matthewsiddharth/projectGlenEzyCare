package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnRegisterNav = findViewById(R.id.btnRegisterNav);
        Button btnLoginNav = findViewById(R.id.btnLoginNav);

        btnRegisterNav.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLoginNav.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, LoginDetailsActivity.class));
        });
    }
}