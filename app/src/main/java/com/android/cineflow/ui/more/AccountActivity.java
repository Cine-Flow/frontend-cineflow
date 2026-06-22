package com.android.cineflow.ui.more;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.SubscriptionDto;
import com.android.cineflow.data.network.dto.UserProfileDto;
import com.android.cineflow.data.network.dto.UpdateProfileRequestDto;
import com.android.cineflow.data.network.dto.ChangePasswordRequestDto;
import com.android.cineflow.data.settings.SettingsManager;

import java.io.File;
import java.util.Locale;

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

public class AccountActivity extends com.android.cineflow.ui.base.BaseActivity {

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvStats;
    private TextView tvSubscription;
    private UserProfileDto currentProfile;
    
    private TextView tvSelectedQuality;
    private TextView tvSelectedLanguage;
    private SwitchCompat switchAutoplay;
    private SwitchCompat switchNotifications;
    private TextView tvCacheSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        bindSettings();
        loadProfileData();
        handleDeepLinking();
    }

    private void handleDeepLinking() {
        String targetSection = getIntent().getStringExtra("target_section");
        if (targetSection != null) {
            android.widget.ScrollView scrollView = findViewById(R.id.scroll_view);
            if (scrollView != null) {
                scrollView.post(() -> {
                    View targetView = null;
                    if ("settings".equals(targetSection)) {
                        targetView = findViewById(R.id.section_video_settings);
                    } else if ("support".equals(targetSection)) {
                        targetView = findViewById(R.id.section_support);
                    } else if ("terms".equals(targetSection)) {
                        targetView = findViewById(R.id.row_terms);
                    }
                    if (targetView != null) {
                        scrollView.scrollTo(0, targetView.getTop());
                        if ("terms".equals(targetSection)) {
                            targetView.performClick();
                        }
                    }
                });
            }
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvStats = findViewById(R.id.tv_stats);
        tvSubscription = findViewById(R.id.tv_subscription);

        tvSelectedQuality = findViewById(R.id.tv_selected_quality);
        tvSelectedLanguage = findViewById(R.id.tv_selected_language);
        switchAutoplay = findViewById(R.id.switch_autoplay);
        switchNotifications = findViewById(R.id.switch_notifications);
        tvCacheSize = findViewById(R.id.tv_cache_size);

        // Back button action
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Edit Profile Quick Action
        findViewById(R.id.layout_profile_name_edit).setOnClickListener(v -> showEditProfileDialog());

        // Edit Profile row action
        findViewById(R.id.row_edit_profile).setOnClickListener(v -> showEditProfileDialog());

        // Change Password row action
        findViewById(R.id.row_change_password).setOnClickListener(v -> showChangePasswordDialog());

        // Video Quality row action
        findViewById(R.id.row_video_quality).setOnClickListener(v -> showQualityDialog());

        // Language row action
        findViewById(R.id.row_language).setOnClickListener(v -> showLanguageDialog());

        // Clear Cache row action
        findViewById(R.id.row_clear_cache).setOnClickListener(v -> performClearCache());

        // Terms row action
        findViewById(R.id.row_terms).setOnClickListener(v -> showTermsDialog());

        // Support row action
        findViewById(R.id.row_support).setOnClickListener(v -> showSupportDialog());
    }

    private void bindSettings() {
        SettingsManager settings = SettingsManager.getInstance();
        if (settings == null) return;

        String quality = settings.getVideoQuality();
        if (SettingsManager.QUALITY_AUTO.equals(quality)) {
            tvSelectedQuality.setText(R.string.quality_auto);
        } else {
            tvSelectedQuality.setText(quality);
        }
        switchAutoplay.setChecked(settings.isAutoplayEnabled());
        switchNotifications.setChecked(settings.isNotificationsEnabled());

        // Display current language
        String currentLang = settings.getLanguage();
        if (SettingsManager.LANG_ENGLISH.equals(currentLang)) {
            tvSelectedLanguage.setText(R.string.language_english);
        } else {
            tvSelectedLanguage.setText(R.string.language_vietnamese);
        }

        // Switch change listeners
        switchAutoplay.setOnCheckedChangeListener((buttonView, isChecked) -> 
            settings.setAutoplayEnabled(isChecked));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setNotificationsEnabled(isChecked);
            if (isChecked) {
                triggerLocalWelcomeNotification();
            }
        });

        // Initial cache size display
        updateCacheSizeText();
    }

    private void loadProfileData() {
        ApiClient.getFilmApiService().getProfile().enqueue(new Callback<ApiResponseDto<UserProfileDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<UserProfileDto>> call, Response<ApiResponseDto<UserProfileDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindProfile(response.body().getData());
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<UserProfileDto>> call, Throwable t) {
                showError();
            }
        });
    }

    private void bindProfile(UserProfileDto profile) {
        this.currentProfile = profile;
        tvName.setText(profile.getFullName() != null ? profile.getFullName() : profile.getUsername());
        tvEmail.setText(profile.getEmail());
        if (tvPhone != null) {
            tvPhone.setText(profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()
                    ? getString(R.string.account_phone_prefix, profile.getPhoneNumber())
                    : getString(R.string.account_phone_not_set));
        }
        tvStats.setText(getString(R.string.account_stats_format, profile.getFavoriteCount(), profile.getWatchHistoryCount()));
        
        SubscriptionDto subscription = profile.getCurrentSubscription();
        tvSubscription.setText(subscription != null
                ? getString(R.string.account_subscription_format, subscription.getPackageName())
                : getString(R.string.account_no_subscription));
    }

    private void showError() {
        Toast.makeText(this, R.string.toast_load_account_error, Toast.LENGTH_SHORT).show();
    }

    // --- Quality Selection Dialog ---
    private void showQualityDialog() {
        SettingsManager settings = SettingsManager.getInstance();
        if (settings == null) return;

        final String[] qualities = {
                SettingsManager.QUALITY_AUTO,
                SettingsManager.QUALITY_FHD,
                SettingsManager.QUALITY_HD,
                SettingsManager.QUALITY_SD
        };

        final String[] qualityLabels = {
                getString(R.string.quality_auto),
                SettingsManager.QUALITY_FHD,
                SettingsManager.QUALITY_HD,
                SettingsManager.QUALITY_SD
        };

        String currentQuality = settings.getVideoQuality();
        int checkedItem = 0;
        for (int i = 0; i < qualities.length; i++) {
            if (qualities[i].equals(currentQuality)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_quality_title)
                .setSingleChoiceItems(qualityLabels, checkedItem, (dialog, which) -> {
                    String selectedQuality = qualities[which];
                    settings.setVideoQuality(selectedQuality);
                    
                    String displayQuality = selectedQuality.equals(SettingsManager.QUALITY_AUTO) 
                            ? getString(R.string.quality_auto) 
                            : selectedQuality;
                    tvSelectedQuality.setText(displayQuality);
                    dialog.dismiss();
                    Toast.makeText(this, getString(R.string.dialog_quality_updated, displayQuality), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // --- Language Selection Dialog ---
    private void showLanguageDialog() {
        SettingsManager settings = SettingsManager.getInstance();
        if (settings == null) return;

        final String[] langLabels = {
                getString(R.string.language_vietnamese),
                getString(R.string.language_english)
        };
        final String[] langCodes = {
                SettingsManager.LANG_VIETNAMESE,
                SettingsManager.LANG_ENGLISH
        };

        String currentLang = settings.getLanguage();
        int checkedItem = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_language_title)
                .setSingleChoiceItems(langLabels, checkedItem, (dialog, which) -> {
                    String selectedCode = langCodes[which];
                    if (!selectedCode.equals(settings.getLanguage())) {
                        settings.setLanguage(selectedCode);
                        tvSelectedLanguage.setText(langLabels[which]);
                        dialog.dismiss();
                        Toast.makeText(this, getString(R.string.toast_language_changed, langLabels[which]), Toast.LENGTH_SHORT).show();
                        // Recreate the activity to apply new locale
                        recreate();
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // --- Cache Operations ---
    private void updateCacheSizeText() {
        long cacheSize = 0;
        try {
            cacheSize += getFolderSize(getCacheDir());
            cacheSize += getFolderSize(getCodeCacheDir());
        } catch (Exception ignored) {}

        double sizeInMb = (double) cacheSize / (1024 * 1024);
        tvCacheSize.setText(String.format(Locale.getDefault(), "%.1f MB", sizeInMb));
    }

    private long getFolderSize(File file) {
        long size = 0;
        if (file == null || !file.exists()) return 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    size += getFolderSize(subFile);
                }
            }
        } else {
            size = file.length();
        }
        return size;
    }

    private void performClearCache() {
        try {
            boolean success = deleteDir(getCacheDir());
            if (success) {
                updateCacheSizeText();
                Toast.makeText(this, R.string.toast_cache_cleared, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_cache_error, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_cache_exception, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    // --- Terms & Support Premium Dialogs ---
    private void showTermsDialog() {
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.terms_content));
        textView.setTextColor(getResources().getColor(R.color.text_primary));
        textView.setTextSize(14f);
        textView.setLineSpacing(4f, 1.1f);
        
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setPadding(48, 32, 48, 32);
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_terms_title)
                .setView(scrollView)
                .setPositiveButton(R.string.agree, null)
                .show();
    }

    private void showSupportDialog() {
        final String[] options = {getString(R.string.support_email_option), getString(R.string.support_hotline_option)};
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_support_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        sendSupportEmail();
                    } else if (which == 1) {
                        callSupportHotline();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void sendSupportEmail() {
        android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.setData(android.net.Uri.parse("mailto:support@cineflow.com"));
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject, tvName.getText().toString()));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.support_email_body));
        try {
            startActivity(android.content.Intent.createChooser(emailIntent, getString(R.string.support_email_chooser)));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.toast_no_email_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void callSupportHotline() {
        android.content.Intent dialIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
        dialIntent.setData(android.net.Uri.parse("tel:19001234"));
        try {
            startActivity(dialIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.toast_no_phone_support, Toast.LENGTH_SHORT).show();
        }
    }

    // --- Edit Profile Dialog ---
    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etFullName = dialogView.findViewById(R.id.et_full_name);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etPhoneNumber = dialogView.findViewById(R.id.et_phone_number);

        // Pre-fill the current display name and phone number
        if (currentProfile != null) {
            etFullName.setText(currentProfile.getFullName() != null ? currentProfile.getFullName() : "");
            etEmail.setText(currentProfile.getEmail() != null ? currentProfile.getEmail() : "");
            etPhoneNumber.setText(currentProfile.getPhoneNumber() != null ? currentProfile.getPhoneNumber() : "");
        } else {
            etFullName.setText(tvName.getText().toString());
            etEmail.setText(tvEmail.getText().toString());
        }
        etFullName.setSelection(etFullName.getText().length());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Safe check for background transparent corner styling
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String newName = etFullName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPhone = etPhoneNumber.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, R.string.toast_name_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (newEmail.isEmpty()) {
                Toast.makeText(this, R.string.toast_email_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, R.string.toast_email_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            performUpdateProfileApi(newName, newEmail, newPhone, dialog);
        });

        dialog.show();
    }

    private void performUpdateProfileApi(String newName, String newEmail, String newPhone, AlertDialog dialog) {
        UpdateProfileRequestDto request = new UpdateProfileRequestDto(newName, newEmail, newPhone);
        ApiClient.getFilmApiService().updateProfile(request).enqueue(new Callback<ApiResponseDto<UserProfileDto>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<UserProfileDto>> call, Response<ApiResponseDto<UserProfileDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    dialog.dismiss();
                    Toast.makeText(AccountActivity.this, R.string.toast_profile_updated, Toast.LENGTH_SHORT).show();
                    bindProfile(response.body().getData());
                } else {
                    Toast.makeText(AccountActivity.this, R.string.toast_profile_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<UserProfileDto>> call, Throwable t) {
                Toast.makeText(AccountActivity.this, R.string.toast_server_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Change Password Dialog ---
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String oldPass = etOldPassword.getText().toString();
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, R.string.toast_password_min_length, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, R.string.toast_password_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }

            performChangePasswordApi(oldPass, newPass, dialog);
        });

        dialog.show();
    }

    private void performChangePasswordApi(String oldPass, String newPass, AlertDialog dialog) {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(oldPass, newPass);
        ApiClient.getFilmApiService().changePassword(request).enqueue(new Callback<ApiResponseDto<Void>>() {
            @Override
            public void onResponse(Call<ApiResponseDto<Void>> call, Response<ApiResponseDto<Void>> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(AccountActivity.this, R.string.toast_password_changed, Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = getString(R.string.toast_password_error);
                    try {
                        if (response.errorBody() != null) {
                            String errStr = response.errorBody().string();
                            if (errStr.contains("Mật khẩu cũ không chính xác")) {
                                errorMsg = getString(R.string.toast_password_old_wrong);
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AccountActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponseDto<Void>> call, Throwable t) {
                Toast.makeText(AccountActivity.this, R.string.toast_server_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void triggerLocalWelcomeNotification() {
        android.content.Context context = this;
        String channelId = "cineflow_notifications";
        
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    getString(R.string.notification_channel_name),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.notification_channel_desc));
            notificationManager.createNotificationChannel(channel);
        }

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_movies)
                .setContentTitle(getString(R.string.notification_welcome_title))
                .setContentText(getString(R.string.notification_welcome_text))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}
