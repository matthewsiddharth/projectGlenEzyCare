package com.example.glenezycareapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<UserModel> patientList;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(UserModel patient);
    }

    public PatientAdapter(List<UserModel> patientList, OnPatientClickListener listener) {
        this.patientList = patientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        UserModel patient = patientList.get(position);
        holder.tvPatientName.setText(patient.getFullName());
        holder.tvPatientEmail.setText(patient.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onPatientClick(patient));
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvPatientEmail;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientEmail = itemView.findViewById(R.id.tvPatientEmail);
        }
    }
}
