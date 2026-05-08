// ==============================
// QueueStatusActivity.java
// ==============================

package com.example.glenezycareapps;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class QueueStatusActivity extends AppCompatActivity {

    TextView tvCurrentQueue;
    android.widget.ImageView btnBack;
    android.widget.Button btnRefreshQueue;

    DatabaseReference queueRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_status);

        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);
        btnBack = findViewById(R.id.btnBack);
        btnRefreshQueue = findViewById(R.id.btnRefreshQueue);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnRefreshQueue != null) {
            btnRefreshQueue.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Status Refreshed", android.widget.Toast.LENGTH_SHORT).show();
                // addValueEventListener already provides real-time updates,
                // but we can manually trigger a log or re-fetch if needed.
            });
        }

        queueRef = FirebaseDatabase.getInstance()
                .getReference("queue/currentQueue");

        queueRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        Integer current =
                                snapshot.getValue(Integer.class);

                        if(current != null) {

                            tvCurrentQueue.setText(
                                    "Q" + String.format("%03d",
                                            current));
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                    }
                });
    }
}