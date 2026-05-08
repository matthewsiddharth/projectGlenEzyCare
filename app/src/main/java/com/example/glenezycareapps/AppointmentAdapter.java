package com.example.glenezycareapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<AppointmentModel> appointmentList;
    private boolean showPatientName;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(AppointmentModel appointment);
    }

    public AppointmentAdapter(List<AppointmentModel> appointmentList, boolean showPatientName, OnAppointmentClickListener listener) {
        this.appointmentList = appointmentList;
        this.showPatientName = showPatientName;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        AppointmentModel appointment = appointmentList.get(position);
        holder.tvDoctorName.setText(appointment.getDoctor());
        holder.tvSpecialty.setText(appointment.getSpecialty());
        holder.tvDate.setText(appointment.getDate());
        holder.tvTime.setText(appointment.getTime());
        holder.tvStatus.setText(appointment.getStatus());

        // Color coding for status monitoring
        if ("Serving".equals(appointment.getStatus())) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
        } else if ("Completed".equals(appointment.getStatus())) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3")); // Blue
        } else if ("Cancelled".equals(appointment.getStatus())) {
            holder.tvStatus.setTextColor(android.graphics.Color.RED);
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#FFA726")); // Orange for Pending
        }

        if (showPatientName) {
            holder.tvPatientName.setVisibility(View.VISIBLE);
            holder.tvPatientName.setText("Patient: " + appointment.getPatientName());
        } else {
            holder.tvPatientName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvSpecialty, tvDate, tvTime, tvStatus, tvPatientName;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
        }
    }
}
