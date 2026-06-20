package com.android.cineflow.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.RegisterRequestDto;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class RegisterActivity extends com.android.cineflow.ui.base.BaseActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnRegister;
    private TextView tvError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.login_signup);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvError = findViewById(R.id.tv_error);
        progressBar = findViewById(R.id.progress_bar);

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tvError.setVisibility(View.GONE);

        if (username.isEmpty()) { etUsername.setError(getString(R.string.register_err_username_required)); return; }
        if (email.isEmpty()) { etEmail.setError(getString(R.string.forgot_password_email_required)); return; }
        if (password.isEmpty()) { etPassword.setError(getString(R.string.login_err_password_required)); return; }

        setLoading(true);

        FilmApiService api = ApiClient.getFilmApiService();
        api.register(new RegisterRequestDto(username, email, password))
                .enqueue(new Callback<ApiResponseDto<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponseDto<Void>> call,
                                           Response<ApiResponseDto<Void>> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String msg = extractErrorMessage(response);
                            if (msg.toLowerCase().contains("email")) etEmail.setError(msg);
                            showError(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                        setLoading(false);
                        showError(getString(R.string.reset_password_network_error, t.getMessage()));
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnRegister.setAlpha(loading ? 0.5f : 1f);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private String extractErrorMessage(Response<ApiResponseDto<Void>> response) {
        if (response.body() != null && response.body().getMessage() != null) {
            return response.body().getMessage();
        }

        if (response.errorBody() != null) {
            try {
                ApiResponseDto<?> error = new Gson().fromJson(response.errorBody().string(), ApiResponseDto.class);
                if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                    return error.getMessage();
                }
            } catch (IOException | JsonSyntaxException ignored) {
                // Fall through to a status-aware message.
            }
        }

        if (response.code() == 409) return getString(R.string.register_err_email_exists);
        return getString(R.string.register_err_failed);
    }
}
