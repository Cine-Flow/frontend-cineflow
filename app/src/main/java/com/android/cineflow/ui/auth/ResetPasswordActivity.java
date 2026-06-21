package com.android.cineflow.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.ResetPasswordRequestDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;
import com.google.gson.Gson;

import java.io.IOException;

public class ResetPasswordActivity extends com.android.cineflow.ui.base.BaseActivity {
    private static final String TAG = "ResetPasswordActivity";

    private TextInputEditText etToken;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnReset;
    private TextView tvError;
    private TextView tvSuccess;
    private ProgressBar progressBar;
    private final Gson gson = new Gson();
    private boolean resetInProgress = false;

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
        Uri deepLink = getIntent().getData();
        if ((tokenExtra == null || tokenExtra.isEmpty()) && deepLink != null) {
            tokenExtra = deepLink.getQueryParameter("token");
        }
        if (tokenExtra != null && !tokenExtra.isEmpty()) {
            etToken.setText(tokenExtra);
        }

        btnReset.setOnClickListener(v -> attemptReset());
    }

    public void onResetPasswordClicked(View view) {
        attemptReset();
    }

    private void attemptReset() {
        if (resetInProgress) {
            return;
        }

        String token = normalizeResetToken(etToken.getText() != null ? etToken.getText().toString().trim() : "");
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        // Client-side validation
        if (token.isEmpty()) {
            etToken.setError(getString(R.string.reset_password_token_required));
            Toast.makeText(this, R.string.reset_password_token_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.isEmpty()) {
            etNewPassword.setError(getString(R.string.reset_password_password_required));
            Toast.makeText(this, R.string.reset_password_password_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            etNewPassword.setError(getString(R.string.reset_password_password_min));
            Toast.makeText(this, R.string.reset_password_password_min, Toast.LENGTH_SHORT).show();
            return;
        }
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(getString(R.string.reset_password_confirm_required));
            Toast.makeText(this, R.string.reset_password_confirm_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.reset_password_mismatch));
            Toast.makeText(this, R.string.reset_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        tvSuccess.setText(R.string.reset_password_processing);
        tvSuccess.setVisibility(View.VISIBLE);
        Toast.makeText(this, R.string.reset_password_processing, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Submitting password reset request");

        try {
            submitReset(ApiClient.getPublicFilmApiService(), token, newPassword, confirmPassword);
        } catch (RuntimeException e) {
            setLoading(false);
            String errorMessage = getString(R.string.reset_password_network_error, e.getMessage());
            tvError.setText(errorMessage);
            tvError.setVisibility(View.VISIBLE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void submitReset(FilmApiService api, String token, String newPassword, String confirmPassword) {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(token, newPassword, confirmPassword);
        api.resetPassword(request).enqueue(new Callback<ApiResponseDto<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<Void>> call,
                                   Response<ApiResponseDto<Void>> response) {
                setLoading(false);
                boolean success = response.isSuccessful()
                        && (response.body() == null || response.body().isSuccess());
                if (success) {
                    tvSuccess.setText(R.string.reset_password_success);
                    tvSuccess.setVisibility(View.VISIBLE);
                    btnReset.setEnabled(false);
                    Toast.makeText(ResetPasswordActivity.this, R.string.reset_password_success_short, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = getErrorMessage(response);
                    Log.w(TAG, "Password reset failed with HTTP " + response.code() + ": " + errorMessage);
                    tvError.setText(errorMessage);
                    tvError.setVisibility(View.VISIBLE);
                    Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                setLoading(false);
                String errorMessage = getString(R.string.reset_password_network_error, t.getMessage());
                Log.e(TAG, "Password reset network failure", t);
                tvError.setText(errorMessage);
                tvError.setVisibility(View.VISIBLE);
                Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String normalizeResetToken(String rawToken) {
        if (rawToken == null) {
            return "";
        }

        String token = rawToken.trim();
        int tokenParamIndex = token.indexOf("token=");
        if (tokenParamIndex >= 0) {
            String tokenValue = token.substring(tokenParamIndex + "token=".length());
            int ampersandIndex = tokenValue.indexOf('&');
            token = ampersandIndex >= 0 ? tokenValue.substring(0, ampersandIndex) : tokenValue;
        }
        return token.trim();
    }

    private void setLoading(boolean loading) {
        resetInProgress = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnReset.setEnabled(!loading);
        btnReset.setAlpha(loading ? 0.5f : 1f);
        btnReset.setText(loading ? R.string.reset_password_processing_button : R.string.reset_password_btn);
    }

    private String getErrorMessage(Response<ApiResponseDto<Void>> response) {
        if (response.body() != null && response.body().getMessage() != null) {
            return response.body().getMessage();
        }

        if (response.errorBody() != null) {
            try {
                ApiResponseDto<?> errorResponse = gson.fromJson(response.errorBody().string(), ApiResponseDto.class);
                if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                    return errorResponse.getMessage();
                }
            } catch (IOException | RuntimeException ignored) {
                // Fall through to the localized generic message.
            }
        }

        return getString(R.string.reset_password_error);
    }
}
