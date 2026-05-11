package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvSlogan = findViewById(R.id.tvSlogan);
        
        // Hide slogan initially for animation
        tvSlogan.setAlpha(0f);
        
        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        
        // Animate App Name slightly
        tvAppName.setAlpha(0f);
        tvAppName.animate().alpha(1f).setDuration(1500).start();

        // Start slogan animation after 1.2 seconds delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvSlogan.startAnimation(fadeIn);
        }, 1200);

        // Transition to LoginActivity after 4.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 4500);
    }
}
