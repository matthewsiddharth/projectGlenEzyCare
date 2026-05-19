package com.example.glenezycareapps;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel> notificationList;

    public interface OnNotificationActionListener {
        void onAction(NotificationModel notification, String status);
    }

    private OnNotificationActionListener listener;

    public NotificationAdapter(List<NotificationModel> notificationList, OnNotificationActionListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        
        String timeAgo = DateUtils.getRelativeTimeSpanString(notification.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        holder.tvTime.setText(timeAgo);

        if (notification.getType() != null) {
            if (notification.getType().equals("cancellation")) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_delete);
                holder.ivIcon.setColorFilter(0xFFD32F2F);
            } else if (notification.getType().equals("reminder")) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_popup_reminder);
                holder.ivIcon.setColorFilter(0xFF1976D2);
            } else {
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivIcon.setColorFilter(0xFF4CAF50);
            }
        }
        
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

        // Handle Actions for reminders
        if ("reminder".equals(notification.getType())) {
            if (notification.getActionStatus() == null) {
                holder.llActions.setVisibility(View.VISIBLE);
                holder.tvActionTaken.setVisibility(View.GONE);
                
                holder.btnConfirm.setOnClickListener(v -> listener.onAction(notification, "confirmed"));
                holder.btnDecline.setOnClickListener(v -> listener.onAction(notification, "declined"));
            } else {
                holder.llActions.setVisibility(View.GONE);
                holder.tvActionTaken.setVisibility(View.VISIBLE);
                holder.tvActionTaken.setText("Status: " + notification.getActionStatus());
                if (notification.getActionStatus().equals("confirmed")) {
                    holder.tvActionTaken.setTextColor(0xFF4CAF50);
                } else {
                    holder.tvActionTaken.setTextColor(0xFFF44336);
                }
            }
        } else {
            holder.llActions.setVisibility(View.GONE);
            holder.tvActionTaken.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime, tvActionTaken;
        ImageView ivIcon;
        View unreadIndicator;
        LinearLayout llActions;
        Button btnConfirm, btnDecline;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            llActions = itemView.findViewById(R.id.llActions);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            tvActionTaken = itemView.findViewById(R.id.tvActionTaken);
        }
    }
}
