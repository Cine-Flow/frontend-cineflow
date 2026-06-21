package com.android.cineflow.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    private AdminUserDto editing;
    private OnUserSavedListener listener;

    public static UserFormDialogFragment newInstance(@Nullable AdminUserDto user) {
        UserFormDialogFragment f = new UserFormDialogFragment();
        f.editing = user;
        return f;
    }

    public void setOnUserSavedListener(OnUserSavedListener l) {
        this.listener = l;
    }

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
        RadioGroup rgRole = dialog.findViewById(R.id.rg_role);
        RadioButton rbRoleUser = dialog.findViewById(R.id.rb_role_user);
        RadioButton rbRoleAdmin = dialog.findViewById(R.id.rb_role_admin);
        ImageView ivPreview = dialog.findViewById(R.id.iv_avatar_preview);
        TextView tvInitial = dialog.findViewById(R.id.tv_avatar_preview_initial);

        tvTitle.setText(isEdit ? R.string.admin_dialog_edit_user : R.string.admin_dialog_create_user);
        layoutPassword.setVisibility(isEdit ? View.GONE : View.VISIBLE);

        if (isEdit) {
            etUsername.setText(editing.getUsername());
            etEmail.setText(editing.getEmail());
            etFullName.setText(editing.getFullName());
            etPhone.setText(editing.getPhoneNumber());
            etAvatarUrl.setText(editing.getAvatarUrl());
            tvRoleReadonly.setText(getString(R.string.admin_user_role_format,
                    getString("ROLE_ADMIN".equals(editing.getRole()) ? R.string.form_role_admin : R.string.form_role_user), editing.getId()));
            if ("ROLE_ADMIN".equals(editing.getRole())) {
                rbRoleAdmin.setChecked(true);
            } else {
                rbRoleUser.setChecked(true);
            }
        } else {
            tvRoleReadonly.setText(R.string.admin_user_role_choose);
            rbRoleUser.setChecked(true);
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
            String password = etPassword.getText().toString();
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
            if (!isEdit && password.length() < 6) {
                etPassword.setError(getString(R.string.admin_err_min_6_chars));
                return;
            }

            String role = rgRole.getCheckedRadioButtonId() == R.id.rb_role_admin
                    ? "ROLE_ADMIN" : "ROLE_USER";
            String passwordValue = isEdit ? null : password;
            if (listener != null) {
                listener.onUserSaved(new AdminUserRequestDto(
                        username, email, passwordValue, fullName, phone, avatar, role), editing);
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
