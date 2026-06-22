package com.android.cineflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.FilmApiService;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.LoginRequestDto;
import com.android.cineflow.data.network.dto.LoginResponseDto;
import com.android.cineflow.ui.admin.AdminDashboardActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class LoginActivity extends com.android.cineflow.ui.base.BaseActivity {

    private TextInputEditText etIdentifier;
    private TextInputEditText etPassword;
    private MaterialButton btnSignIn;
    private TextView tvError;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;
    private TextView tvRegister;

    private FilmApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        api = ApiClient.getFilmApiService();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        etIdentifier = findViewById(R.id.et_identifier);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        tvError = findViewById(R.id.tv_error);
        progressBar = findViewById(R.id.progress_bar);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);

        btnSignIn.setOnClickListener(v -> attemptLogin());

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String identifier = etIdentifier.getText() != null ? etIdentifier.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tvError.setVisibility(View.GONE);

        if (identifier.isEmpty()) {
            etIdentifier.setError(getString(R.string.login_err_identifier_required));
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.login_err_password_required));
            return;
        }

        setLoading(true);

        LoginRequestDto request = new LoginRequestDto(identifier, password);
        api.login(request).enqueue(new Callback<ApiResponseDto<LoginResponseDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<LoginResponseDto>> call,
                                   Response<ApiResponseDto<LoginResponseDto>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    LoginResponseDto data = response.body().getData();
                    AuthManager auth = AuthManager.getInstance();
                    auth.saveSession(
                            data.getAccessToken(),
                            data.getRefreshToken(),
                            data.getId(),
                            data.getUsername(),
                            data.getEmail(),
                            data.getRole()
                    );
                    if (auth.isAdmin()) {
                        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else if (isTaskRoot()) {
                        // Launched as a fresh task (e.g. from admin logout) with no
                        // caller to return to — go to MainActivity instead of just
                        // finishing, otherwise the app would exit entirely.
                        Intent mainIntent = new Intent(LoginActivity.this, com.android.cineflow.MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    String msg = getString(R.string.login_err_failed);
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.code() == 401) {
                        msg = getString(R.string.login_err_invalid_credentials);
                    }
                    showError(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<LoginResponseDto>> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.reset_password_network_error, t.getMessage()));
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignIn.setEnabled(!loading);
        btnSignIn.setAlpha(loading ? 0.5f : 1f);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
