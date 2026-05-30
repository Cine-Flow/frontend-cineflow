package com.android.cineflow.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.CreateFilmRequestDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.network.dto.UpdateFilmRequestDto;
import com.android.cineflow.data.repository.AdminFilmRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class FilmFormDialogFragment extends DialogFragment {

    private static final String ARG_FILM_ID = "film_id";
    private static final String ARG_FILM_TITLE = "film_title";
    private static final String ARG_FILM_DESC = "film_desc";
    private static final String ARG_FILM_THUMB = "film_thumb";
    private static final String ARG_FILM_TRAILER = "film_trailer";
    private static final String ARG_FILM_YEAR = "film_year";
    private static final String ARG_FILM_PREMIUM = "film_premium";
    private static final String ARG_FILM_TYPE = "film_type";

    private boolean isEdit;
    private int editFilmId;
    private String selectedType = "SINGLE";

    public static FilmFormDialogFragment newInstance(@Nullable FilmDetailDto film) {
        FilmFormDialogFragment fragment = new FilmFormDialogFragment();
        if (film != null) {
            Bundle args = new Bundle();
            args.putInt(ARG_FILM_ID, film.getId());
            args.putString(ARG_FILM_TITLE, film.getTitle());
            args.putString(ARG_FILM_DESC, film.getDescription());
            args.putString(ARG_FILM_THUMB, film.getThumbnailUrl());
            args.putString(ARG_FILM_TRAILER, film.getTrailerUrl());
            args.putInt(ARG_FILM_YEAR, film.getReleaseYear());
            args.putBoolean(ARG_FILM_PREMIUM, film.getIsPremium());
            args.putString(ARG_FILM_TYPE, film.getType());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_Cineflow_Admin);
        dialog.setContentView(R.layout.dialog_film_form);

        Bundle args = getArguments();
        isEdit = args != null && args.containsKey(ARG_FILM_ID);
        if (isEdit) {
            editFilmId = args.getInt(ARG_FILM_ID);
            selectedType = args.getString(ARG_FILM_TYPE, "SINGLE");
        }

        TextView tvDialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        EditText etTitle = dialog.findViewById(R.id.et_title);
        EditText etDescription = dialog.findViewById(R.id.et_description);
        EditText etThumbnailUrl = dialog.findViewById(R.id.et_thumbnail_url);
        EditText etTrailerUrl = dialog.findViewById(R.id.et_trailer_url);
        EditText etReleaseYear = dialog.findViewById(R.id.et_release_year);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_type);
        SwitchMaterial switchPremium = dialog.findViewById(R.id.switch_premium);

        tvDialogTitle.setText(isEdit ? "Edit Film" : "Create Film");

        if (isEdit && args != null) {
            etTitle.setText(args.getString(ARG_FILM_TITLE, ""));
            etDescription.setText(args.getString(ARG_FILM_DESC, ""));
            etThumbnailUrl.setText(args.getString(ARG_FILM_THUMB, ""));
            etTrailerUrl.setText(args.getString(ARG_FILM_TRAILER, ""));
            int year = args.getInt(ARG_FILM_YEAR, 0);
            etReleaseYear.setText(year > 0 ? String.valueOf(year) : "");
            switchPremium.setChecked(args.getBoolean(ARG_FILM_PREMIUM, false));
            selectedType = args.getString(ARG_FILM_TYPE, "SINGLE");
        }

        String[] types = {"SINGLE", "SERIES", "LIVE"};
        String[] typeLabels = {"Movie", "Series", "Live"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_dark, typeLabels);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_dark);
        spinnerType.setAdapter(spinnerAdapter);

        int typeIndex = 0;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(selectedType)) { typeIndex = i; break; }
        }
        spinnerType.setSelection(typeIndex);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = types[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());

        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }

            String description = etDescription.getText().toString().trim();
            String thumbnailUrl = etThumbnailUrl.getText().toString().trim();
            String trailerUrl = etTrailerUrl.getText().toString().trim();
            String yearStr = etReleaseYear.getText().toString().trim();
            Integer year = yearStr.isEmpty() ? null : Integer.parseInt(yearStr);
            boolean isPremium = switchPremium.isChecked();

            AdminFilmRepository repo = AdminFilmRepository.getInstance();

            if (isEdit) {
                UpdateFilmRequestDto request = new UpdateFilmRequestDto(
                        title, description, thumbnailUrl, trailerUrl, year, isPremium, selectedType);
                repo.updateFilm(editFilmId, request, new AdminFilmRepository.OnResultListener() {
                    @Override public void onSuccess(FilmDetailDto film) { dismiss(); }
                    @Override public void onError(String message) {}
                });
            } else {
                CreateFilmRequestDto request = new CreateFilmRequestDto(
                        title, description, thumbnailUrl, trailerUrl, year, isPremium, selectedType);
                repo.createFilm(request, new AdminFilmRepository.OnResultListener() {
                    @Override public void onSuccess(FilmDetailDto film) { dismiss(); }
                    @Override public void onError(String message) {}
                });
            }
        });

        return dialog;
    }
}
