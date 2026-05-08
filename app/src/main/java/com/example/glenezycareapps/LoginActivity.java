package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button btnLoginNav, btnRegisterNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLoginNav = findViewById(R.id.btnLoginNav);
        btnRegisterNav = findViewById(R.id.btnRegisterNav);

        btnLoginNav.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, LoginDetailsActivity.class));
        });

        btnRegisterNav.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
