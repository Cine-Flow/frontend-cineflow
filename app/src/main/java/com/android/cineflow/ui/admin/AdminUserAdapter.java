package com.android.cineflow.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    /**
     * Mirrors the backend {@code users} row plus the user's current subscription state
     * (derived from {@code user_subscriptions} on the server).
     */
    public static class MockUser {
        public String id;             // UUID
        public String username;
        public String email;
        public String fullName;       // nullable
        public String phoneNumber;    // nullable
        public String avatarUrl;      // nullable
        public String role;           // ROLE_USER | ROLE_ADMIN
        public String createdAt;      // display string
        public String subscriptionPlan;   // null = free; else package name
        public String subscriptionExpiry; // display string, null when no sub

        public MockUser(String id, String username, String email, String fullName,
                        String phoneNumber, String avatarUrl, String role,
                        String createdAt, String subscriptionPlan, String subscriptionExpiry) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.avatarUrl = avatarUrl;
            this.role = role;
            this.createdAt = createdAt;
            this.subscriptionPlan = subscriptionPlan;
            this.subscriptionExpiry = subscriptionExpiry;
        }
    }

    public interface OnUserActionListener {
        void onEditUser(MockUser user);
        void onResetPassword(MockUser user);
        void onDeleteUser(MockUser user);
    }

    private List<MockUser> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public AdminUserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<MockUser> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() { return users.size(); }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvAvatarInitial;
        private final TextView tvName;
        private final TextView tvRole;
        private final TextView tvUsername;
        private final TextView tvEmail;
        private final TextView tvSubscription;
        private final TextView tvJoined;
        private final ImageView btnEdit;
        private final ImageView btnReset;
        private final ImageView btnDelete;

        UserViewHolder(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.iv_avatar);
            tvAvatarInitial = v.findViewById(R.id.tv_avatar_initial);
            tvName = v.findViewById(R.id.tv_user_name);
            tvRole = v.findViewById(R.id.tv_user_role);
            tvUsername = v.findViewById(R.id.tv_user_username);
            tvEmail = v.findViewById(R.id.tv_user_email);
            tvSubscription = v.findViewById(R.id.tv_user_subscription);
            tvJoined = v.findViewById(R.id.tv_user_joined);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnReset = v.findViewById(R.id.btn_reset);
            btnDelete = v.findViewById(R.id.btn_delete);
        }

        void bind(MockUser user) {
            String display = (user.fullName != null && !user.fullName.isEmpty())
                    ? user.fullName : user.username;
            tvName.setText(display);
            tvUsername.setText("@" + user.username);
            tvEmail.setText(user.email);
            tvJoined.setText("Joined " + user.createdAt);

            // Avatar — load URL if present, else fall back to a neutral circle with initials
            int placeholderColor = itemView.getContext().getColor(R.color.surface_tertiary);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(placeholderColor);
            ivAvatar.setBackground(circle);

            if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                tvAvatarInitial.setText("");
                Glide.with(itemView.getContext())
                        .load(user.avatarUrl)
                        .circleCrop()
                        .placeholder(R.color.surface_tertiary)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageDrawable(null);
                tvAvatarInitial.setText(initial(display));
            }

            // Role chip — read-only
            boolean admin = "ROLE_ADMIN".equals(user.role);
            tvRole.setText(admin ? "ADMIN" : "USER");
            int roleColor = itemView.getContext().getColor(
                    admin ? R.color.brand_primary : R.color.surface_tertiary);
            GradientDrawable roleBg = new GradientDrawable();
            roleBg.setCornerRadius(24f);
            roleBg.setColor(roleColor);
            tvRole.setBackground(roleBg);
            tvRole.setTextColor(itemView.getContext().getColor(
                    admin ? R.color.text_primary : R.color.text_secondary));

            // Subscription
            if (user.subscriptionPlan != null) {
                tvSubscription.setText(user.subscriptionPlan + " · until " + user.subscriptionExpiry);
                tvSubscription.setTextColor(
                        itemView.getContext().getColor(R.color.badge_premium));
            } else {
                tvSubscription.setText("Free tier");
                tvSubscription.setTextColor(
                        itemView.getContext().getColor(R.color.text_tertiary));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditUser(user);
            });
            btnReset.setOnClickListener(v -> {
                if (listener != null) listener.onResetPassword(user);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteUser(user);
            });
        }

        private String initial(String name) {
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0))
                        .toUpperCase();
            }
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        }
    }
}
