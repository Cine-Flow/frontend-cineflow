package com.android.cineflow.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.ForgotPasswordRequestDto;
import com.android.cineflow.data.settings.SettingsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class ForgotPasswordActivity extends com.android.cineflow.ui.base.BaseActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnSend;
    private TextView tvError;
    private TextView tvSuccess;
    private ProgressBar progressBar;
    private TextView tvHaveToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etEmail = findViewById(R.id.et_email);
        btnSend = findViewById(R.id.btn_send);
        tvError = findViewById(R.id.tv_error);
        tvSuccess = findViewById(R.id.tv_success);
        progressBar = findViewById(R.id.progress_bar);
        tvHaveToken = findViewById(R.id.tv_have_token);

        btnSend.setOnClickListener(v -> attemptSend());

        tvHaveToken.setOnClickListener(v -> {
            startActivity(new Intent(this, ResetPasswordActivity.class));
        });
    }

    private void attemptSend() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.forgot_password_email_required));
            return;
        }

        setLoading(true);
        FilmApiService api = ApiClient.getFilmApiService();
        api.forgotPassword(new ForgotPasswordRequestDto(email))
                .enqueue(new Callback<ApiResponseDto<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<Void>> call,
                                           Response<ApiResponseDto<Void>> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            tvSuccess.setText(R.string.forgot_password_success);
                            tvSuccess.setVisibility(View.VISIBLE);
                            // Show the "have token" link more prominently after success
                            tvHaveToken.setVisibility(View.VISIBLE);
                        } else {
                            String msg = getString(R.string.forgot_password_error);
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
        btnSend.setEnabled(!loading);
        btnSend.setAlpha(loading ? 0.5f : 1f);
    }
}
