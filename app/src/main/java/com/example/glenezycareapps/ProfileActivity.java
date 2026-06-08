package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone;
    Button btnEditProfile;
    ImageView btnBack;
    CircleImageView ivProfilePic;
    FloatingActionButton btnEditPic;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBack = findViewById(R.id.btnBack);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEditPic = findViewById(R.id.btnEditPic);

        // Hide edit pic button in view mode
        btnEditPic.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);
        
        loadUserProfile();

        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });
    }

    private void loadUserProfile() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String profilePicUrl = snapshot.child("profilePic").getValue(String.class);

                    etFullName.setText(name);
                    etEmail.setText(email);
                    etPhone.setText(phone != null ? phone : "");

                    if (profilePicUrl != null && !profilePicUrl.isEmpty() && !isDestroyed()) {
                        Glide.with(ProfileActivity.this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.iconprofile)
                                .into(ivProfilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}