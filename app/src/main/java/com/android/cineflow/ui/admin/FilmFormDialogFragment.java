package com.android.cineflow.ui.admin;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.ContentUriRequestBody;
import com.android.cineflow.data.network.Response;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.CreateFilmRequestDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.network.dto.UpdateFilmRequestDto;
import com.android.cineflow.data.repository.AdminFilmRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FilmFormDialogFragment extends DialogFragment {

    private static final String ARG_FILM_ID = "film_id";
    private static final String ARG_FILM_TITLE = "film_title";
    private static final String ARG_FILM_DESC = "film_desc";
    private static final String ARG_FILM_THUMB = "film_thumb";
    private static final String ARG_FILM_TRAILER = "film_trailer";
    private static final String ARG_FILM_YEAR = "film_year";
    private static final String ARG_FILM_PREMIUM = "film_premium";
    private static final String ARG_FILM_TYPE = "film_type";
    private static final String ARG_FILM_VIDEO_URL = "film_video_url";

    private boolean isEdit;
    private int editFilmId;
    private String selectedType = "SINGLE";

    // Upload target tracking
    private EditText currentUploadTarget;
    private String currentUploadFolder;

    private LinearLayout layoutVideoUrl;
    private com.google.android.material.button.MaterialButton btnManageEpisodes;
    private LinearLayout layoutUploadProgress;
    private TextView tvUploadStatus;
    private TextView tvTrailerLabel;

    // File pickers
    private ActivityResultLauncher<Intent> filePickerLauncher;

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
            // Get videoUrl from first episode if exists
            if (film.getEpisodes() != null && !film.getEpisodes().isEmpty()) {
                args.putString(ARG_FILM_VIDEO_URL, film.getEpisodes().get(0).getVideoUrl());
            }
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null && currentUploadTarget != null) {
                            uploadFileStreaming(fileUri, currentUploadFolder, currentUploadTarget);
                        }
                    }
                }
        );
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
        EditText etVideoUrl = dialog.findViewById(R.id.et_video_url);
        Spinner spinnerType = dialog.findViewById(R.id.spinner_type);
        SwitchMaterial switchPremium = dialog.findViewById(R.id.switch_premium);

        layoutVideoUrl = dialog.findViewById(R.id.layout_video_url);
        btnManageEpisodes = dialog.findViewById(R.id.btn_manage_episodes);
        layoutUploadProgress = dialog.findViewById(R.id.layout_upload_progress);
        tvUploadStatus = dialog.findViewById(R.id.tv_upload_status);
        tvTrailerLabel = dialog.findViewById(R.id.tv_trailer_label);

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
            String videoUrl = args.getString(ARG_FILM_VIDEO_URL, "");
            etVideoUrl.setText(videoUrl);
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
                updateTypeVisibility();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Initial visibility
        updateTypeVisibility();

        // Upload buttons
        dialog.findViewById(R.id.btn_upload_thumbnail).setOnClickListener(v -> {
            currentUploadTarget = etThumbnailUrl;
            currentUploadFolder = "thumbnails";
            openFilePicker("image/*");
        });

        dialog.findViewById(R.id.btn_upload_trailer).setOnClickListener(v -> {
            currentUploadTarget = etTrailerUrl;
            currentUploadFolder = "trailers";
            openFilePicker("video/*");
        });

        dialog.findViewById(R.id.btn_upload_video).setOnClickListener(v -> {
            currentUploadTarget = etVideoUrl;
            currentUploadFolder = "films";
            openFilePicker("video/*");
        });

        // Manage episodes button
        btnManageEpisodes.setOnClickListener(v -> {
            if (isEdit) {
                AdminEpisodesDialogFragment episodesDialog =
                        AdminEpisodesDialogFragment.newInstance(editFilmId, args.getString(ARG_FILM_TITLE, ""));
                episodesDialog.show(getParentFragmentManager(), "episodes_manager");
            } else {
                Toast.makeText(requireContext(), "Vui lòng lưu phim trước khi quản lý tập", Toast.LENGTH_SHORT).show();
            }
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
            String videoUrl = etVideoUrl.getText().toString().trim();
            String yearStr = etReleaseYear.getText().toString().trim();
            Integer year = yearStr.isEmpty() ? null : Integer.parseInt(yearStr);
            boolean isPremium = switchPremium.isChecked();

            AdminFilmRepository repo = AdminFilmRepository.getInstance();

            if (isEdit) {
                UpdateFilmRequestDto request = new UpdateFilmRequestDto(
                        title, description, thumbnailUrl, trailerUrl, year, isPremium, selectedType, videoUrl);
                repo.updateFilm(editFilmId, request, new AdminFilmRepository.OnResultListener() {
                    @Override public void onSuccess(FilmDetailDto film) { dismiss(); }
                    @Override public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                CreateFilmRequestDto request = new CreateFilmRequestDto(
                        title, description, thumbnailUrl, trailerUrl, year, isPremium, selectedType, videoUrl);
                repo.createFilm(request, new AdminFilmRepository.OnResultListener() {
                    @Override public void onSuccess(FilmDetailDto film) {
                        boolean shouldOpenEpisodes = "SERIES".equals(selectedType)
                                && film != null
                                && film.getId() != null;
                        if (shouldOpenEpisodes) {
                            AdminEpisodesDialogFragment episodesDialog =
                                    AdminEpisodesDialogFragment.newInstance(film.getId(), film.getTitle());
                            dismiss();
                            episodesDialog.show(getParentFragmentManager(), "episodes_manager");
                        } else {
                            dismiss();
                        }
                    }
                    @Override public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return dialog;
    }

    private void updateTypeVisibility() {
        if (tvTrailerLabel != null) {
            tvTrailerLabel.setText("LIVE".equals(selectedType) ? "Live stream URL" : "Trailer URL");
        }
        if ("SINGLE".equals(selectedType)) {
            layoutVideoUrl.setVisibility(View.VISIBLE);
            btnManageEpisodes.setVisibility(View.GONE);
        } else if ("SERIES".equals(selectedType)) {
            layoutVideoUrl.setVisibility(View.GONE);
            btnManageEpisodes.setVisibility(View.VISIBLE);
        } else {
            layoutVideoUrl.setVisibility(View.GONE);
            btnManageEpisodes.setVisibility(View.GONE);
        }
    }

    private void openFilePicker(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    private void uploadFileStreaming(Uri fileUri, String folder, EditText targetEditText) {
        layoutUploadProgress.setVisibility(View.VISIBLE);
        tvUploadStatus.setText("Dang tai len...");

        try {
            ContentResolver resolver = requireContext().getContentResolver();
            String mimeType = resolver.getType(fileUri);
            if (mimeType == null) mimeType = "application/octet-stream";

            String fileName = "upload_file";
            long fileSize = -1L;
            Cursor cursor = resolver.query(fileUri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                    if (sizeIndex >= 0) {
                        fileSize = cursor.getLong(sizeIndex);
                    }
                }
                cursor.close();
            }

            if (fileSize == 0L) {
                showUploadError("Khong the doc tep tin");
                return;
            }

            RequestBody requestBody = new ContentUriRequestBody(resolver, fileUri, mimeType, fileSize);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestBody);

            ApiClient.getFilmApiService().uploadFile(body, folder).enqueue(new Callback<ApiResponseDto<String>>() {
                @Override
                public void onResponse(Call<ApiResponseDto<String>> call, Response<ApiResponseDto<String>> response) {
                    layoutUploadProgress.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        targetEditText.setText(response.body().getData());
                        Toast.makeText(requireContext(), "Tai len thanh cong!", Toast.LENGTH_SHORT).show();
                    } else {
                        showUploadError("Upload that bai: HTTP " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseDto<String>> call, Throwable t) {
                    layoutUploadProgress.setVisibility(View.GONE);
                    showUploadError("Loi ket noi: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            showUploadError("Loi: " + e.getMessage());
        }
    }

    private void uploadFile(Uri fileUri, String folder, EditText targetEditText) {
        layoutUploadProgress.setVisibility(View.VISIBLE);
        tvUploadStatus.setText("Đang tải lên...");

        try {
            ContentResolver resolver = requireContext().getContentResolver();
            String mimeType = resolver.getType(fileUri);
            if (mimeType == null) mimeType = "application/octet-stream";

            // Get file name
            String fileName = "upload_file";
            Cursor cursor = resolver.query(fileUri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }

            // Copy URI content to temp file
            InputStream inputStream = resolver.openInputStream(fileUri);
            if (inputStream == null) {
                showUploadError("Không thể đọc tệp tin");
                return;
            }

            File tempFile = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            inputStream.close();

            // Create multipart request
            RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestBody);

            // Call upload API
            ApiClient.getFilmApiService().uploadFile(body, folder).enqueue(new Callback<ApiResponseDto<String>>() {
                @Override
                public void onResponse(Call<ApiResponseDto<String>> call, Response<ApiResponseDto<String>> response) {
                    layoutUploadProgress.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        String uploadedUrl = response.body().getData();
                        targetEditText.setText(uploadedUrl);
                        Toast.makeText(requireContext(), "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        showUploadError("Upload thất bại: HTTP " + response.code());
                    }
                    // Cleanup temp file
                    tempFile.delete();
                }

                @Override
                public void onFailure(Call<ApiResponseDto<String>> call, Throwable t) {
                    layoutUploadProgress.setVisibility(View.GONE);
                    showUploadError("Lỗi kết nối: " + t.getMessage());
                    tempFile.delete();
                }
            });

        } catch (Exception e) {
            showUploadError("Lỗi: " + e.getMessage());
        }
    }

    private void showUploadError(String message) {
        layoutUploadProgress.setVisibility(View.GONE);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
