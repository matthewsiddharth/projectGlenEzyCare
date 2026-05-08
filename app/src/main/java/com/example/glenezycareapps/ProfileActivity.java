package com.example.glenezycareapps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone;
    Button btnUpdateProfile;
    ImageView btnBack;
    CircleImageView ivProfilePic;
    FloatingActionButton btnEditPic;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String userId;
    Uri imageUri;
    ProgressDialog progressDialog;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfilePic.setImageURI(imageUri);
                    uploadProfilePicture();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnBack = findViewById(R.id.btnBack);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEditPic = findViewById(R.id.btnEditPic);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("profile_pics").child(userId + ".jpg");

        loadUserProfile();

        btnBack.setOnClickListener(v -> onBackPressed());
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnEditPic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(profilePicUrl)
                                .placeholder(android.R.drawable.ic_menu_myplaces)
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

    private void uploadProfilePicture() {
        if (imageUri != null) {
            progressDialog.setMessage("Uploading picture...");
            progressDialog.show();

            storageReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        databaseReference.child("profilePic").setValue(downloadUrl)
                                .addOnCompleteListener(dbTask -> {
                                    progressDialog.dismiss();
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed to update database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProfile() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("phone", phone);

        databaseReference.updateChildren(updates).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}