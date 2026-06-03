package com.android.cineflow.ui.premierleague;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.FavoriteDto;
import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.ui.base.BaseFragment;
import com.android.cineflow.ui.detail.FilmDetailActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremierLeagueFragment extends BaseFragment {

    private PremierLeagueViewModel viewModel;
    private PremierLeagueAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_premier_league;
    }

    @Override
    protected void initViews(View view) {
        ListView lvPremierLeague = view.findViewById(R.id.lv_premier_league);
        adapter = new PremierLeagueAdapter(requireContext(), new ArrayList<>(),
                mode -> viewModel.expandSection(mode),
                (mode, apiDate, displayDate) -> viewModel.loadMatchesForDate(mode, apiDate, displayDate),
                card -> {
                    Intent intent = new Intent(requireContext(), FilmDetailActivity.class);
                    intent.putExtra(FilmDetailActivity.EXTRA_FILM_ID, card.getId());
                    startActivity(intent);
                });
        lvPremierLeague.setAdapter(adapter);
    }

    private void addPremierLeagueFavorite(ContentCard card, ImageView favoriteButton) {
        AuthManager authManager = AuthManager.getInstance();
        if (authManager == null || !authManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer filmId = parseFilmId(card.getId());
        if (filmId == null) {
            Toast.makeText(requireContext(), "Không thể thêm nội dung này vào yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        favoriteButton.setEnabled(false);
        ApiClient.getFilmApiService().addFavorite(filmId).enqueue(new Callback<ApiResponseDto<FavoriteDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<FavoriteDto>> call,
                                   Response<ApiResponseDto<FavoriteDto>> response) {
                favoriteButton.setEnabled(true);
                if (response.isSuccessful()) {
                    favoriteButton.setImageResource(R.drawable.ic_heart_filled);
                    Toast.makeText(requireContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<FavoriteDto>> call, Throwable t) {
                favoriteButton.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi mạng khi thêm yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer parseFilmId(String id) {
        try {
            return Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(PremierLeagueViewModel.class);
        viewModel.getSections().observe(getViewLifecycleOwner(), sections -> {
            adapter.setSections(sections);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}
