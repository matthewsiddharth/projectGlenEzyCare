package com.example.glenezycareapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<UserModel> staffList;
    private OnStaffClickListener listener;

    public interface OnStaffClickListener {
        void onStaffClick(UserModel staff);
        void onDeleteClick(UserModel staff);
    }

    public StaffAdapter(List<UserModel> staffList, OnStaffClickListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        UserModel staff = staffList.get(position);
        holder.tvStaffName.setText(staff.getFullName());
        holder.tvStaffEmail.setText(staff.getEmail());
        
        String roleInfo = staff.getRole().toUpperCase();
        if (staff.getSpecialty() != null && !staff.getSpecialty().isEmpty()) {
            roleInfo += " - " + staff.getSpecialty();
        }
        holder.tvStaffRole.setText(roleInfo);
        
        holder.itemView.setOnClickListener(v -> listener.onStaffClick(staff));
        holder.btnDeleteStaff.setOnClickListener(v -> listener.onDeleteClick(staff));
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvStaffName, tvStaffEmail, tvStaffRole;
        View btnDeleteStaff;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStaffName = itemView.findViewById(R.id.tvStaffName);
            tvStaffEmail = itemView.findViewById(R.id.tvStaffEmail);
            tvStaffRole = itemView.findViewById(R.id.tvStaffRole);
            btnDeleteStaff = itemView.findViewById(R.id.btnDeleteStaff);
        }
    }
}
