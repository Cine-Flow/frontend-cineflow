package com.android.cineflow.ui.more;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.SubscriptionDto;
import com.android.cineflow.data.network.dto.UserProfileDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ApiClient.getFilmApiService().getProfile().enqueue(new Callback<ApiResponseDto<UserProfileDto>>() {
            @Override public void onResponse(Call<ApiResponseDto<UserProfileDto>> call, Response<ApiResponseDto<UserProfileDto>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showError(); return;
                }
                bind(response.body().getData());
            }
            @Override public void onFailure(Call<ApiResponseDto<UserProfileDto>> call, Throwable t) { showError(); }
        });
    }

    private void bind(UserProfileDto profile) {
        ((TextView) findViewById(R.id.tv_name)).setText(
                profile.getFullName() != null ? profile.getFullName() : profile.getUsername());
        ((TextView) findViewById(R.id.tv_email)).setText(profile.getEmail());
        ((TextView) findViewById(R.id.tv_stats)).setText(
                "Yêu thích: " + profile.getFavoriteCount() + "   Lịch sử: " + profile.getWatchHistoryCount());
        SubscriptionDto subscription = profile.getCurrentSubscription();
        ((TextView) findViewById(R.id.tv_subscription)).setText(subscription != null
                ? "Gói hiện tại: " + subscription.getPackageName()
                : "Chưa đăng ký gói dịch vụ");
    }

    private void showError() {
        Toast.makeText(this, "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
    }
}
