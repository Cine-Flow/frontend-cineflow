package com.android.cineflow.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.AdminCategoryDto;
import com.android.cineflow.data.network.dto.AdminCategoryRequestDto;

public class CategoryFormDialogFragment extends DialogFragment {

    public interface OnCategorySavedListener {
        void onCategorySaved(AdminCategoryRequestDto request, @Nullable AdminCategoryDto editing);
    }

    private AdminCategoryDto editing;
    private OnCategorySavedListener listener;

    public static CategoryFormDialogFragment newInstance(@Nullable AdminCategoryDto category) {
        CategoryFormDialogFragment f = new CategoryFormDialogFragment();
        f.editing = category;
        return f;
    }

    public void setOnCategorySavedListener(OnCategorySavedListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_Cineflow_Admin);
        dialog.setContentView(R.layout.dialog_category_form);

        boolean isEdit = editing != null;
        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextView tvMeta = dialog.findViewById(R.id.tv_meta);
        EditText etName = dialog.findViewById(R.id.et_name);
        EditText etDescription = dialog.findViewById(R.id.et_description);

        tvTitle.setText(isEdit ? R.string.admin_dialog_edit_category : R.string.admin_dialog_create_category);
        if (isEdit) {
            etName.setText(editing.getName());
            etDescription.setText(editing.getDescription());
            long filmCount = editing.getFilmCount();
            tvMeta.setText(getString(R.string.admin_category_meta_format, editing.getId(), filmCount, filmCount == 1 ? "" : "s"));
        } else {
            tvMeta.setText(R.string.admin_category_new);
        }

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError(getString(R.string.admin_err_name_required));
                return;
            }
            if (name.length() > 255) {
                etName.setError(getString(R.string.admin_err_max_255));
                return;
            }

            if (listener != null) {
                listener.onCategorySaved(new AdminCategoryRequestDto(
                        name, description.isEmpty() ? null : description), editing);
            }
            dismiss();
        });

        return dialog;
    }
}
