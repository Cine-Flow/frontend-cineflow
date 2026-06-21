package com.android.cineflow.ui.admin;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.ContentUriRequestBody;
import com.android.cineflow.data.network.Response;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.CreateEpisodeRequestDto;
import com.android.cineflow.data.network.dto.EpisodeDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AdminEpisodesDialogFragment extends DialogFragment {

    private static final String ARG_FILM_ID = "film_id";
    private static final String ARG_FILM_TITLE = "film_title";

    private int filmId;
    private String filmTitle;
    private List<EpisodeDto> episodes = new ArrayList<>();
    private EpisodeListAdapter adapter;
    private ProgressBar progressBar;

    private EditText pendingVideoUrlField;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    public static AdminEpisodesDialogFragment newInstance(int filmId, String filmTitle) {
        AdminEpisodesDialogFragment fragment = new AdminEpisodesDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FILM_ID, filmId);
        args.putString(ARG_FILM_TITLE, filmTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filmId = getArguments().getInt(ARG_FILM_ID);
            filmTitle = getArguments().getString(ARG_FILM_TITLE, "");
        }

        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null && pendingVideoUrlField != null) {
                            uploadEpisodeVideoStreaming(fileUri, pendingVideoUrlField);
                        }
                    }
                }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_Cineflow_Admin);
        dialog.setContentView(R.layout.dialog_episodes_manager);

        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        tvTitle.setText(getString(R.string.admin_episode_title, filmTitle));

        progressBar = dialog.findViewById(R.id.progress_bar);
        RecyclerView rvEpisodes = dialog.findViewById(R.id.rv_episodes);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EpisodeListAdapter();
        rvEpisodes.setAdapter(adapter);

        dialog.findViewById(R.id.btn_add_episode).setOnClickListener(v -> showEpisodeFormDialog(null));
        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        loadEpisodes();
        return dialog;
    }

    private void loadEpisodes() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getFilmApiService().getFilmById(filmId).enqueue(new Callback<ApiResponseDto<FilmDetailDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FilmDetailDto>> call, Response<ApiResponseDto<FilmDetailDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<EpisodeDto> latestEpisodes = response.body().getData().getEpisodes();
                    episodes = latestEpisodes != null ? latestEpisodes : new ArrayList<>();
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Không tải được tập phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FilmDetailDto>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi tải tập phim", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEpisodeFormDialog(@Nullable EpisodeDto episode) {
        boolean isEdit = episode != null;
        Dialog formDialog = new Dialog(requireContext(), R.style.Theme_Cineflow_Admin);
        formDialog.setContentView(R.layout.dialog_episode_form);

        TextView tvFormTitle = formDialog.findViewById(R.id.tv_form_title);
        EditText etEpNumber = formDialog.findViewById(R.id.et_episode_number);
        EditText etEpTitle = formDialog.findViewById(R.id.et_episode_title);
        EditText etEpVideoUrl = formDialog.findViewById(R.id.et_episode_video_url);

        tvFormTitle.setText(isEdit ? "Sửa tập" : "Thêm tập mới");
        if (isEdit) {
            etEpNumber.setText(String.valueOf(episode.getEpisodeNumber()));
            etEpTitle.setText(episode.getTitle() != null ? episode.getTitle() : "");
            etEpVideoUrl.setText(episode.getVideoUrl() != null ? episode.getVideoUrl() : "");
        } else {
            int nextEpNumber = episodes.stream()
                    .map(EpisodeDto::getEpisodeNumber)
                    .filter(number -> number != null)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
            etEpNumber.setText(String.valueOf(nextEpNumber));
            etEpTitle.setText(getString(R.string.admin_episode_default_title, nextEpNumber));
        }

        formDialog.findViewById(R.id.btn_upload_ep_video).setOnClickListener(v -> {
            pendingVideoUrlField = etEpVideoUrl;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            videoPickerLauncher.launch(intent);
        });

        formDialog.findViewById(R.id.btn_ep_cancel).setOnClickListener(v -> formDialog.dismiss());

        formDialog.findViewById(R.id.btn_ep_save).setOnClickListener(v -> {
            String epNumStr = etEpNumber.getText().toString().trim();
            String epTitle = etEpTitle.getText().toString().trim();
            String epVideoUrl = etEpVideoUrl.getText().toString().trim();

            if (epNumStr.isEmpty() || epTitle.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tập và tiêu đề", Toast.LENGTH_SHORT).show();
                return;
            }

            int epNumber;
            try {
                epNumber = Integer.parseInt(epNumStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tập không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            CreateEpisodeRequestDto request = new CreateEpisodeRequestDto(epNumber, epTitle, epVideoUrl, 2700);
            if (isEdit) {
                updateEpisode(episode.getId(), request, formDialog);
            } else {
                createEpisode(request, formDialog);
            }
        });

        formDialog.show();
    }

    private void createEpisode(CreateEpisodeRequestDto request, Dialog formDialog) {
        ApiClient.getFilmApiService().createEpisode(filmId, request).enqueue(new Callback<ApiResponseDto<EpisodeDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<EpisodeDto>> call, Response<ApiResponseDto<EpisodeDto>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã thêm tập", Toast.LENGTH_SHORT).show();
                    formDialog.dismiss();
                    loadEpisodes();
                } else {
                    Toast.makeText(requireContext(), "Thêm tập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<EpisodeDto>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEpisode(int episodeId, CreateEpisodeRequestDto request, Dialog formDialog) {
        ApiClient.getFilmApiService().updateEpisode(episodeId, request).enqueue(new Callback<ApiResponseDto<EpisodeDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<EpisodeDto>> call, Response<ApiResponseDto<EpisodeDto>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã cập nhật tập", Toast.LENGTH_SHORT).show();
                    formDialog.dismiss();
                    loadEpisodes();
                } else {
                    Toast.makeText(requireContext(), "Cập nhật tập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<EpisodeDto>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEpisode(EpisodeDto episode) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa tập phim")
                .setMessage("Bạn có chắc muốn xóa \"" + episode.getTitle() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> {
                    ApiClient.getFilmApiService().deleteEpisode(episode.getId()).enqueue(new Callback<ApiResponseDto<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadEpisodes();
                            } else {
                                Toast.makeText(requireContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void uploadEpisodeVideoStreaming(Uri fileUri, EditText targetField) {
        try {
            ContentResolver resolver = requireContext().getContentResolver();
            String mimeType = resolver.getType(fileUri);
            if (mimeType == null) mimeType = "video/mp4";

            String fileName = "episode_video.mp4";
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
                Toast.makeText(requireContext(), "Khong the doc tep tin", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestBody = new ContentUriRequestBody(resolver, fileUri, mimeType, fileSize);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestBody);

            Toast.makeText(requireContext(), "Dang tai len...", Toast.LENGTH_SHORT).show();
            ApiClient.getFilmApiService().uploadFile(body, "films").enqueue(new Callback<ApiResponseDto<String>>() {
                @Override
                public void onResponse(Call<ApiResponseDto<String>> call, Response<ApiResponseDto<String>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        targetField.setText(response.body().getData());
                        Toast.makeText(requireContext(), "Upload thanh cong!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Upload that bai: HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseDto<String>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Loi ket noi upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Loi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadEpisodeVideo(Uri fileUri, EditText targetField) {
        try {
            ContentResolver resolver = requireContext().getContentResolver();
            String mimeType = resolver.getType(fileUri);
            if (mimeType == null) mimeType = "video/mp4";

            String fileName = "episode_video.mp4";
            Cursor cursor = resolver.query(fileUri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }

            InputStream inputStream = resolver.openInputStream(fileUri);
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Không thể đọc tệp tin", Toast.LENGTH_SHORT).show();
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

            RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestBody);

            Toast.makeText(requireContext(), "Đang tải lên...", Toast.LENGTH_SHORT).show();
            ApiClient.getFilmApiService().uploadFile(body, "films").enqueue(new Callback<ApiResponseDto<String>>() {
                @Override
                public void onResponse(Call<ApiResponseDto<String>> call, Response<ApiResponseDto<String>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        targetField.setText(response.body().getData());
                        Toast.makeText(requireContext(), "Upload thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Upload thất bại", Toast.LENGTH_SHORT).show();
                    }
                    tempFile.delete();
                }

                @Override
                public void onFailure(Call<ApiResponseDto<String>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Lỗi kết nối upload", Toast.LENGTH_SHORT).show();
                    tempFile.delete();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    class EpisodeListAdapter extends RecyclerView.Adapter<EpisodeListAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_episode, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            EpisodeDto ep = episodes.get(position);
            holder.tvNumber.setText(getString(R.string.admin_episode_number, ep.getEpisodeNumber()));
            holder.tvTitle.setText(ep.getTitle() != null ? ep.getTitle() : "");
            boolean hasVideo = ep.getVideoUrl() != null && !ep.getVideoUrl().isEmpty();
            holder.tvStatus.setText(hasVideo ? "Có video" : "Chưa có video");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(
                    hasVideo ? R.color.status_success : R.color.text_tertiary));
            holder.btnEdit.setOnClickListener(v -> showEpisodeFormDialog(ep));
            holder.btnDelete.setOnClickListener(v -> deleteEpisode(ep));
        }

        @Override
        public int getItemCount() {
            return episodes.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvNumber, tvTitle, tvStatus;
            ImageView btnEdit, btnDelete;

            VH(View v) {
                super(v);
                tvNumber = v.findViewById(R.id.tv_ep_number);
                tvTitle = v.findViewById(R.id.tv_ep_title);
                tvStatus = v.findViewById(R.id.tv_ep_status);
                btnEdit = v.findViewById(R.id.btn_ep_edit);
                btnDelete = v.findViewById(R.id.btn_ep_delete);
            }
        }
    }
}
