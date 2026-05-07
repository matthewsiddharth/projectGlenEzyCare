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

        TextView tvSlogan = findViewById(R.id.tvSlogan);
        Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        
        // Start animation after 1 second delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvSlogan.setVisibility(View.VISIBLE);
            tvSlogan.startAnimation(fadeInUp);
        }, 1000);

        // 5 second delay (5000 milliseconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 5000);
    }
}
