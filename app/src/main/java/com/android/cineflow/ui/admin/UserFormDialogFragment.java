package com.android.cineflow.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.AdminUserDto;
import com.android.cineflow.data.network.dto.AdminUserRequestDto;
import com.bumptech.glide.Glide;

public class UserFormDialogFragment extends DialogFragment {

    public interface OnUserSavedListener {
        void onUserSaved(AdminUserRequestDto request, @Nullable AdminUserDto editing);
    }

    public interface OnSendResetPasswordListener {
        void onSendResetPassword(AdminUserDto user);
    }

    private AdminUserDto editing;
    private OnUserSavedListener listener;
    private OnSendResetPasswordListener resetPasswordListener;

    public static UserFormDialogFragment newInstance(@Nullable AdminUserDto user) {
        UserFormDialogFragment f = new UserFormDialogFragment();
        f.editing = user;
        return f;
    }

    public void setOnUserSavedListener(OnUserSavedListener l) {
        this.listener = l;
    }

    public void setOnSendResetPasswordListener(OnSendResetPasswordListener l) {
        this.resetPasswordListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_Cineflow_Admin);
        dialog.setContentView(R.layout.dialog_user_form);

        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView tvRoleReadonly = dialog.findViewById(R.id.tv_role_readonly);
        EditText etUsername = dialog.findViewById(R.id.et_username);
        EditText etEmail = dialog.findViewById(R.id.et_email);
        EditText etFullName = dialog.findViewById(R.id.et_full_name);
        EditText etPhone = dialog.findViewById(R.id.et_phone);
        EditText etAvatarUrl = dialog.findViewById(R.id.et_avatar_url);
        ImageView ivPreview = dialog.findViewById(R.id.iv_avatar_preview);
        TextView tvInitial = dialog.findViewById(R.id.tv_avatar_preview_initial);
        TextView btnSendResetLink = dialog.findViewById(R.id.btn_send_reset_link);

        tvTitle.setText(R.string.admin_dialog_edit_user);

        if (editing != null) {
            btnSendResetLink.setVisibility(android.view.View.VISIBLE);
            btnSendResetLink.setOnClickListener(v -> {
                if (resetPasswordListener != null) resetPasswordListener.onSendResetPassword(editing);
                dismiss();
            });
        } else {
            btnSendResetLink.setVisibility(android.view.View.GONE);
        }

        if (editing != null) {
            etUsername.setText(editing.getUsername());
            etEmail.setText(editing.getEmail());
            etFullName.setText(editing.getFullName());
            etPhone.setText(editing.getPhoneNumber());
            etAvatarUrl.setText(editing.getAvatarUrl());
            tvRoleReadonly.setText(getString(R.string.admin_user_role_format,
                    getString(R.string.form_role_user), editing.getId()));
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

            if (username.isEmpty()) {
                etUsername.setError(getString(R.string.admin_err_username_required));
                return;
            }
            if (!username.matches("[A-Za-z0-9._-]{3,}")) {
                etUsername.setError(getString(R.string.admin_err_username_required));
                return;
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.admin_err_valid_email_required));
                return;
            }

            if (listener != null && editing != null) {
                listener.onUserSaved(new AdminUserRequestDto(
                        username, email, null, fullName, phone, avatar), editing);
            }
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
