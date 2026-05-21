package com.example.glenezycareapps;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PatientHomeActivity extends AppCompatActivity {

    View btnQueue, btnQueueStatus, btnAppointment, btnHistory, cardUpcoming;
    TextView tvUserName, tvGreeting, tvHomeQueueNum, tvWaitTime, tvHomeConsultationRoom, btnSeeAll;
    TextView tvUpcomingSpecialty, tvUpcomingDoctor, tvUpcomingDateTime;
    ImageView btnMenu, ivProfileTop, btnNotification;
    BottomNavigationView bottomNav;
    ProgressBar pbWait;
    DrawerLayout drawerLayout;
    LinearLayout navHome, navProfile, navAppointments, navLogout;
    
    DatabaseReference rootRef;
    FirebaseAuth mAuth;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private String currentQueueNumber;
    private String currentSpecialty;
    private String lastTimerQueueNum = "";
    private boolean hasUpcomingAppointment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        btnQueue = findViewById(R.id.btnQueue);
        btnQueueStatus = findViewById(R.id.btnQueueStatus);
        btnAppointment = findViewById(R.id.btnAppointment);
        btnHistory = findViewById(R.id.btnHistory);
        btnSeeAll = findViewById(R.id.btnSeeAll);
        cardUpcoming = findViewById(R.id.cardUpcoming);
        
        btnMenu = findViewById(R.id.btnMenu);
        btnNotification = findViewById(R.id.btnNotification);
        ivProfileTop = findViewById(R.id.ivProfileTop);
        tvUserName = findViewById(R.id.tvUserName);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvHomeQueueNum = findViewById(R.id.tvHomeQueueNum);
        tvWaitTime = findViewById(R.id.tvWaitTime);
        tvHomeConsultationRoom = findViewById(R.id.tvHomeConsultationRoom);
        pbWait = findViewById(R.id.pbWait);
        bottomNav = findViewById(R.id.bottomNav);
        drawerLayout = findViewById(R.id.drawerLayout);
        
        tvUpcomingSpecialty = findViewById(R.id.tvUpcomingSpecialty);
        tvUpcomingDoctor = findViewById(R.id.tvUpcomingDoctor);
        tvUpcomingDateTime = findViewById(R.id.tvUpcomingDateTime);
        
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);
        navAppointments = findViewById(R.id.navAppointments);
        navLogout = findViewById(R.id.navLogout);

        rootRef = FirebaseDatabase.getInstance("https://glenezycare-apps-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mAuth = FirebaseAuth.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
            trackNowServing();
            checkAndGenerateReminders();
            loadUpcomingAppointment();
        }

        btnQueue.setOnClickListener(v -> startActivity(new Intent(this, QueueActivity.class)));
        btnQueueStatus.setOnClickListener(v -> startActivity(new Intent(this, QueueStatusActivity.class)));
        btnAppointment.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, AppointmentHistoryActivity.class)));
        btnSeeAll.setOnClickListener(v -> startActivity(new Intent(this, AppointmentHistoryActivity.class)));
        cardUpcoming.setOnClickListener(v -> {
            if (hasUpcomingAppointment) {
                startActivity(new Intent(this, AppointmentHistoryActivity.class));
            } else {
                startActivity(new Intent(this, AppointmentActivity.class));
            }
        });

        ivProfileTop.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navHome.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        navProfile.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, ProfileActivity.class));
        });
        navAppointments.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AppointmentHistoryActivity.class));
        });
        navLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNav();
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String profilePic = snapshot.child("profilePic").getValue(String.class);
                    if (name != null) tvUserName.setText(name);
                    
                    if (profilePic != null && !profilePic.isEmpty() && !isDestroyed()) {
                        Glide.with(PatientHomeActivity.this).load(profilePic).placeholder(R.drawable.iconprofile).into(ivProfileTop);
                    }

                    // Active Queue
                    if (snapshot.hasChild("currentTicket")) {
                        String qNum = snapshot.child("currentTicket").child("queueNumber").getValue(String.class);
                        String specialty = snapshot.child("currentTicket").child("specialty").getValue(String.class);
                        String floor = snapshot.child("currentTicket").child("floor").getValue(String.class);
                        
                        currentQueueNumber = qNum;
                        currentSpecialty = specialty;

                        if (qNum != null) tvHomeQueueNum.setText(qNum);
                        if (specialty != null && floor != null) {
                            tvHomeConsultationRoom.setText(specialty + " - " + floor);
                        }
                        calculateEstimatedWait(qNum, specialty);
                    } else {
                        currentQueueNumber = null;
                        currentSpecialty = null;
                        tvHomeQueueNum.setText("None");
                        tvWaitTime.setText("--:--\nmin");
                        tvHomeConsultationRoom.setText("No active queue");
                        pbWait.setProgress(0);
                        if (countDownTimer != null) countDownTimer.cancel();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void trackNowServing() {
        rootRef.child("queue").child("nowServing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If nowServing changes, re-calculate wait time if ticket exists
                if (currentQueueNumber != null && currentSpecialty != null) {
                    calculateEstimatedWait(currentQueueNumber, currentSpecialty);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateEstimatedWait(String myNumber, String mySpecialty) {
        if (myNumber == null || myNumber.isEmpty() || mySpecialty == null) return;
        
        try {
            int myNumInt;
            if (myNumber.startsWith("Q") && !Character.isLetter(myNumber.charAt(1))) {
                myNumInt = Integer.parseInt(myNumber.substring(1));
            } else {
                myNumInt = Integer.parseInt(myNumber.substring(3));
            }

            rootRef.child("queue").child("nowServing").child(mySpecialty).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    int nowServing = task.getResult().getValue(Integer.class);
                    
                    if (myNumInt <= nowServing) {
                        tvWaitTime.setText("NOW");
                        pbWait.setProgress(100);
                        if (countDownTimer != null) countDownTimer.cancel();
                        return;
                    }

                    // Count how many people are between nowServing and myNumber WITH SAME SPECIALTY
                    rootRef.child("queue").child("tickets").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int peopleAheadSameDept = 0;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String qNumStr = ds.child("queueNumber").getValue(String.class);
                                String spec = ds.child("specialty").getValue(String.class);
                                
                                if (qNumStr != null && spec != null && spec.equals(mySpecialty)) {
                                    int qNum;
                                    if (qNumStr.startsWith("Q") && !Character.isLetter(qNumStr.charAt(1))) {
                                        qNum = Integer.parseInt(qNumStr.substring(1));
                                    } else {
                                        qNum = Integer.parseInt(qNumStr.substring(3));
                                    }

                                    if (qNum > nowServing && qNum < myNumInt) {
                                        peopleAheadSameDept++;
                                    }
                                }
                            }

                            // Calculate wait time: 2 minutes per person ahead
                            long millis = (long) (peopleAheadSameDept + 1) * 2 * 60 * 1000;
                            
                            // Only start if not already counting for this specific ticket
                            if (countDownTimer == null || !myNumber.equals(lastTimerQueueNum)) {
                                lastTimerQueueNum = myNumber;
                                startCountdown(millis);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                } else {
                    // If no one is serving in this specialty yet, wait time is based on myNumInt
                    long millis = (long) myNumInt * 2 * 60 * 1000;
                    if (countDownTimer == null || !myNumber.equals(lastTimerQueueNum)) {
                        lastTimerQueueNum = myNumber;
                        startCountdown(millis);
                    }
                }
            });
        } catch (Exception e) {
            tvWaitTime.setText("--:--\nmin");
        }
    }

    private void startCountdown(long durationMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        final long totalDuration = durationMillis;

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
                
                // Update progress bar based on time
                int progress = (int) (100 - (millisUntilFinished * 100 / totalDuration));
                pbWait.setProgress(progress);
            }

            @Override
            public void onFinish() {
                tvWaitTime.setText("NOW");
                pbWait.setProgress(100);
                sendTurnNotification();
            }
        }.start();
    }

    private void sendTurnNotification() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference notifRef = rootRef.child("notifications").child(uid);
        String notifId = notifRef.push().getKey();
        
        if (notifId != null) {
            NotificationModel turnNotif = new NotificationModel(
                    notifId,
                    uid,
                    "It's Your Turn!",
                    "Your wait time has ended. Please proceed to the consultation room.",
                    System.currentTimeMillis(),
                    "turn_alert",
                    null
            );
            notifRef.child(notifId).setValue(turnNotif);
        }
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvWaitTime.setText(timeFormatted + " min");
    }

    private void loadUpcomingAppointment() {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("appointments").orderByChild("patientId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        AppointmentModel soonest = null;
                        long soonestTime = Long.MAX_VALUE;
                        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy - hh:mm a", Locale.getDefault());
                        
                        Date now = new Date();
                        
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AppointmentModel appointment = ds.getValue(AppointmentModel.class);
                            if (appointment != null && "Pending".equals(appointment.getStatus())) {
                                try {
                                    Date apptDate = sdf.parse(appointment.getDate() + " - " + appointment.getTime());
                                    if (apptDate != null && apptDate.getTime() < soonestTime && apptDate.after(now)) {
                                        soonestTime = apptDate.getTime();
                                        soonest = appointment;
                                    }
                                } catch (ParseException e) {
                                    // Fallback if time format is slightly different
                                }
                            }
                        }
                        
                        if (soonest != null) {
                            hasUpcomingAppointment = true;
                            tvUpcomingSpecialty.setText(soonest.getSpecialty());
                            tvUpcomingDoctor.setText(soonest.getDoctor());
                            tvUpcomingDateTime.setText(soonest.getDate() + " - " + soonest.getTime());
                            cardUpcoming.setVisibility(View.VISIBLE);
                        } else {
                            hasUpcomingAppointment = false;
                            tvUpcomingSpecialty.setText("No upcoming appointment");
                            tvUpcomingDoctor.setText("Click to book now");
                            tvUpcomingDateTime.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void checkAndGenerateReminders() {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("appointments").orderByChild("patientId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                        Calendar cal = Calendar.getInstance();
                        Date today = cal.getTime();
                        
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AppointmentModel appointment = ds.getValue(AppointmentModel.class);
                            if (appointment != null && "Pending".equals(appointment.getStatus())) {
                                try {
                                    Date apptDate = sdf.parse(appointment.getDate());
                                    if (apptDate != null) {
                                        long diff = apptDate.getTime() - today.getTime();
                                        long days = diff / (24 * 60 * 60 * 1000);
                                        
                                        // Generate reminder if appointment is in 1 or 2 days
                                        if (days >= 0 && days <= 2) {
                                            checkIfReminderExists(appointment);
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkIfReminderExists(AppointmentModel appointment) {
        String uid = mAuth.getCurrentUser().getUid();
        rootRef.child("notifications").child(uid).orderByChild("appointmentId").equalTo(appointment.getAppointmentId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean reminderExists = false;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if ("reminder".equals(ds.child("type").getValue(String.class))) {
                                reminderExists = true;
                                break;
                            }
                        }
                        
                        if (!reminderExists) {
                            createReminder(appointment);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void createReminder(AppointmentModel appointment) {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference notifRef = rootRef.child("notifications").child(uid);
        String notifId = notifRef.push().getKey();
        
        if (notifId != null) {
            NotificationModel reminder = new NotificationModel(
                    notifId,
                    uid,
                    "Appointment Reminder",
                    "You have an upcoming appointment for " + appointment.getSpecialty() + " on " + appointment.getDate() + ". Will you be able to make it?",
                    System.currentTimeMillis(),
                    "reminder",
                    appointment.getAppointmentId()
            );
            notifRef.child(notifId).setValue(reminder);
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_appointments) {
                startActivity(new Intent(this, AppointmentHistoryActivity.class));
                return true;
            }
            if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}