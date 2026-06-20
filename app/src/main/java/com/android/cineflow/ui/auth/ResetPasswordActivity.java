package com.android.cineflow.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.ResetPasswordRequestDto;
import com.android.cineflow.data.settings.SettingsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class ResetPasswordActivity extends com.android.cineflow.ui.base.BaseActivity {

    private TextInputEditText etToken;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnReset;
    private TextView tvError;
    private TextView tvSuccess;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etToken = findViewById(R.id.et_token);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnReset = findViewById(R.id.btn_reset);
        tvError = findViewById(R.id.tv_error);
        tvSuccess = findViewById(R.id.tv_success);
        progressBar = findViewById(R.id.progress_bar);

        // Pre-fill token if passed via Intent (e.g., from deep link or ForgotPasswordActivity)
        String tokenExtra = getIntent().getStringExtra("reset_token");
        if (tokenExtra != null && !tokenExtra.isEmpty()) {
            etToken.setText(tokenExtra);
        }

        btnReset.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String token = etToken.getText() != null ? etToken.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        // Client-side validation
        if (token.isEmpty()) {
            etToken.setError(getString(R.string.reset_password_token_required));
            return;
        }
        if (newPassword.isEmpty()) {
            etNewPassword.setError(getString(R.string.reset_password_password_required));
            return;
        }
        if (newPassword.length() < 6) {
            etNewPassword.setError(getString(R.string.reset_password_password_min));
            return;
        }
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(getString(R.string.reset_password_confirm_required));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.reset_password_mismatch));
            return;
        }

        setLoading(true);

        FilmApiService api = ApiClient.getFilmApiService();
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(token, newPassword, confirmPassword);

        api.resetPassword(request).enqueue(new Callback<ApiResponseDto<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<Void>> call,
                                   Response<ApiResponseDto<Void>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    tvSuccess.setText(R.string.reset_password_success);
                    tvSuccess.setVisibility(View.VISIBLE);
                    btnReset.setEnabled(false);

                    // Navigate to LoginActivity after a short delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }, 2000);
                } else {
                    String msg = getString(R.string.reset_password_error);
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    tvError.setText(msg);
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                setLoading(false);
                tvError.setText(getString(R.string.reset_password_network_error, t.getMessage()));
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnReset.setEnabled(!loading);
        btnReset.setAlpha(loading ? 0.5f : 1f);
    }
}
