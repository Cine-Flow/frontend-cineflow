package com.android.cineflow.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.cineflow.R;

public class CategoryFormDialogFragment extends DialogFragment {

    public interface OnCategorySavedListener {
        void onCategorySaved(AdminCategoryAdapter.MockCategory category, boolean isNew);
    }

    private AdminCategoryAdapter.MockCategory editing;
    private OnCategorySavedListener listener;

    public static CategoryFormDialogFragment newInstance(
            @Nullable AdminCategoryAdapter.MockCategory category) {
        CategoryFormDialogFragment f = new CategoryFormDialogFragment();
        f.editing = category;
        return f;
    }

    public void setOnCategorySavedListener(OnCategorySavedListener l) { this.listener = l; }

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

        tvTitle.setText(isEdit ? "Edit Category" : "Create Category");
        if (isEdit) {
            etName.setText(editing.name);
            etDescription.setText(editing.description);
            String filmText = editing.filmCount + " film"
                    + (editing.filmCount == 1 ? "" : "s") + " tagged";
            tvMeta.setText("ID #" + editing.id + " · " + filmText);
        } else {
            tvMeta.setText("New category — ID auto-assigned on save");
        }

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name is required");
                return;
            }
            if (name.length() > 255) {
                etName.setError("Max 255 characters");
                return;
            }

            boolean isNew = !isEdit;
            AdminCategoryAdapter.MockCategory result;
            if (isEdit) {
                editing.name = name;
                editing.description = description.isEmpty() ? null : description;
                result = editing;
            } else {
                result = new AdminCategoryAdapter.MockCategory(
                        null, name, description.isEmpty() ? null : description, 0);
            }
            if (listener != null) listener.onCategorySaved(result, isNew);
            dismiss();
        });

        return dialog;
    }
}
