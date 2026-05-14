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
        
        // Hide initially so they can fade in
        tvAppName.setAlpha(0f);
        tvSlogan.setAlpha(0f);
        
        // Use ViewPropertyAnimator for more reliable animations
        tvAppName.animate()
                .alpha(1f)
                .translationYBy(-20f)
                .setDuration(1000)
                .setStartDelay(300)
                .start();

        tvSlogan.animate()
                .alpha(1f)
                .translationYBy(-20f)
                .setDuration(1000)
                .setStartDelay(1000)
                .start();

        // Transition to LoginActivity after 4.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 4500);
    }
}
