package com.android.cineflow.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.cineflow.R;
import com.bumptech.glide.Glide;

public class UserFormDialogFragment extends DialogFragment {

    public interface OnUserSavedListener {
        void onUserSaved(AdminUserAdapter.MockUser user, boolean isNew);
    }

    private AdminUserAdapter.MockUser editing;
    private OnUserSavedListener listener;

    public static UserFormDialogFragment newInstance(@Nullable AdminUserAdapter.MockUser user) {
        UserFormDialogFragment f = new UserFormDialogFragment();
        f.editing = user;
        return f;
    }

    public void setOnUserSavedListener(OnUserSavedListener l) { this.listener = l; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_Cineflow_Admin);
        dialog.setContentView(R.layout.dialog_user_form);

        boolean isEdit = editing != null;

        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView tvRoleReadonly = dialog.findViewById(R.id.tv_role_readonly);
        EditText etUsername = dialog.findViewById(R.id.et_username);
        EditText etEmail = dialog.findViewById(R.id.et_email);
        EditText etFullName = dialog.findViewById(R.id.et_full_name);
        EditText etPhone = dialog.findViewById(R.id.et_phone);
        EditText etAvatarUrl = dialog.findViewById(R.id.et_avatar_url);
        EditText etPassword = dialog.findViewById(R.id.et_password);
        LinearLayout layoutPassword = dialog.findViewById(R.id.layout_password);
        ImageView ivPreview = dialog.findViewById(R.id.iv_avatar_preview);
        TextView tvInitial = dialog.findViewById(R.id.tv_avatar_preview_initial);

        tvTitle.setText(isEdit ? "Edit User" : "Create User");
        layoutPassword.setVisibility(isEdit ? View.GONE : View.VISIBLE);

        if (isEdit) {
            etUsername.setText(editing.username);
            etEmail.setText(editing.email);
            etFullName.setText(editing.fullName);
            etPhone.setText(editing.phoneNumber);
            etAvatarUrl.setText(editing.avatarUrl);
            tvRoleReadonly.setText("Role: "
                    + ("ROLE_ADMIN".equals(editing.role) ? "Admin" : "User")
                    + " · ID " + editing.id);
        } else {
            tvRoleReadonly.setText("New users are created with the User role.");
        }

        Runnable refreshPreview = () -> {
            String url = etAvatarUrl.getText().toString().trim();
            String nameForInitial = etFullName.getText().toString().trim();
            if (nameForInitial.isEmpty()) {
                nameForInitial = etUsername.getText().toString().trim();
            }
            if (url.isEmpty()) {
                ivPreview.setImageDrawable(null);
                tvInitial.setText(initial(nameForInitial));
            } else {
                tvInitial.setText("");
                Glide.with(requireContext())
                        .load(url)
                        .circleCrop()
                        .placeholder(R.color.surface_tertiary)
                        .into(ivPreview);
            }
        };

        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { refreshPreview.run(); }
        };
        etAvatarUrl.addTextChangedListener(tw);
        etFullName.addTextChangedListener(tw);
        etUsername.addTextChangedListener(tw);

        refreshPreview.run();

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String fullName = nullIfEmpty(etFullName.getText().toString().trim());
            String phone = nullIfEmpty(etPhone.getText().toString().trim());
            String avatar = nullIfEmpty(etAvatarUrl.getText().toString().trim());

            if (username.isEmpty()) { etUsername.setError("Username required"); return; }
            if (!username.matches("[A-Za-z0-9._-]{3,}")) {
                etUsername.setError("3+ chars, letters/digits/._- only"); return;
            }
            if (email.isEmpty() || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                etEmail.setError("Valid email required"); return;
            }
            if (!isEdit && etPassword.getText().toString().length() < 8) {
                etPassword.setError("Min 8 characters"); return;
            }

            AdminUserAdapter.MockUser result;
            boolean isNew = !isEdit;
            if (isEdit) {
                editing.username = username;
                editing.email = email;
                editing.fullName = fullName;
                editing.phoneNumber = phone;
                editing.avatarUrl = avatar;
                result = editing;
            } else {
                result = new AdminUserAdapter.MockUser(
                        "new-" + System.currentTimeMillis(),
                        username, email, fullName, phone, avatar,
                        "ROLE_USER", "Just now",
                        null, null);
            }
            if (listener != null) listener.onUserSaved(result, isNew);
            dismiss();
        });

        return dialog;
    }

    private static String nullIfEmpty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private String initial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return String.valueOf(parts[0].charAt(0)).toUpperCase();
    }
}
