package com.android.cineflow.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FavoriteDto;
import com.android.cineflow.data.network.dto.FilmDto;
import com.android.cineflow.data.network.dto.WatchHistoryDto;
import com.android.cineflow.ui.detail.FilmDetailActivity;
import com.android.cineflow.ui.player.PlayerActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserContentListActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_FAVORITES = "favorites";
    public static final String MODE_LIBRARY = "library";

    private final List<RowItem> items = new ArrayList<>();
    private ContentAdapter adapter;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_content_list);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        ((TextView) findViewById(R.id.tv_title)).setText(
                MODE_LIBRARY.equals(mode) ? "Thư viện" : "Sản phẩm yêu thích");
        ListView listView = findViewById(R.id.lv_items);
        adapter = new ContentAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> open(items.get(position)));
        loadItems();
    }

    private void loadItems() {
        if (MODE_LIBRARY.equals(mode)) {
            ApiClient.getFilmApiService().getWatchHistory().enqueue(new Callback<ApiResponseDto<List<WatchHistoryDto>>>() {
                @Override public void onResponse(Call<ApiResponseDto<List<WatchHistoryDto>>> call, Response<ApiResponseDto<List<WatchHistoryDto>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (WatchHistoryDto history : response.body().getData()) items.add(RowItem.fromHistory(history));
                        adapter.notifyDataSetChanged();
                    } else showError();
                }
                @Override public void onFailure(Call<ApiResponseDto<List<WatchHistoryDto>>> call, Throwable t) { showError(); }
            });
        } else {
            ApiClient.getFilmApiService().getFavorites().enqueue(new Callback<ApiResponseDto<List<FavoriteDto>>>() {
                @Override public void onResponse(Call<ApiResponseDto<List<FavoriteDto>>> call, Response<ApiResponseDto<List<FavoriteDto>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (FavoriteDto favorite : response.body().getData()) items.add(RowItem.fromFavorite(favorite));
                        adapter.notifyDataSetChanged();
                    } else showError();
                }
                @Override public void onFailure(Call<ApiResponseDto<List<FavoriteDto>>> call, Throwable t) { showError(); }
            });
        }
    }

    private void open(RowItem item) {
        if (item.history != null && item.history.getEpisode() != null) {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, item.history.getEpisode().getVideoUrl());
            intent.putExtra(PlayerActivity.EXTRA_EPISODE_ID, item.history.getEpisode().getId());
            intent.putExtra(PlayerActivity.EXTRA_RESUME_POSITION_SECONDS, item.history.getResumePositionSeconds());
            if (item.film != null) {
                intent.putExtra("extra_film_id", item.film.getId());
                intent.putExtra(PlayerActivity.EXTRA_TITLE, item.film.getTitle() + " - " + (item.history.getEpisode().getTitle() != null ? item.history.getEpisode().getTitle() : ("Tập " + item.history.getEpisode().getEpisodeNumber())));
            }
            startActivity(intent);
        } else if (item.film != null) {
            Intent intent = new Intent(this, FilmDetailActivity.class);
            intent.putExtra(FilmDetailActivity.EXTRA_FILM_ID, String.valueOf(item.film.getId()));
            startActivity(intent);
        }
    }

    private void showError() {
        Toast.makeText(this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
    }

    private static class RowItem {
        FilmDto film;
        WatchHistoryDto history;
        String subtitle;
        static RowItem fromFavorite(FavoriteDto favorite) {
            RowItem row = new RowItem(); row.film = favorite.getFilm(); row.subtitle = "Yêu thích"; return row;
        }
        static RowItem fromHistory(WatchHistoryDto history) {
            RowItem row = new RowItem(); row.film = history.getFilm(); row.history = history;
            row.subtitle = "Xem tiếp từ " + history.getResumePositionSeconds() + " giây"; return row;
        }
    }

    private class ContentAdapter extends BaseAdapter {
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(UserContentListActivity.this)
                    .inflate(R.layout.item_user_content, parent, false);
            RowItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.tv_item_title)).setText(item.film != null ? item.film.getTitle() : "");
            ((TextView) convertView.findViewById(R.id.tv_item_subtitle)).setText(item.subtitle);
            Glide.with(UserContentListActivity.this).load(item.film != null ? item.film.getThumbnailUrl() : "")
                    .placeholder(R.drawable.placeholder_card).centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.iv_thumbnail));
            return convertView;
        }
    }
}
