package com.android.cineflow.ui.more;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.android.cineflow.R;
import com.android.cineflow.data.network.ApiClient;
import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.SubscriptionDto;
import com.android.cineflow.data.network.dto.UserProfileDto;
import com.android.cineflow.data.settings.SettingsManager;

import java.io.File;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvStats;
    private TextView tvSubscription;
    
    private TextView tvSelectedQuality;
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
        tvStats = findViewById(R.id.tv_stats);
        tvSubscription = findViewById(R.id.tv_subscription);

        tvSelectedQuality = findViewById(R.id.tv_selected_quality);
        switchAutoplay = findViewById(R.id.switch_autoplay);
        switchNotifications = findViewById(R.id.switch_notifications);
        tvCacheSize = findViewById(R.id.tv_cache_size);

        // Back button action
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Video Quality row action
        findViewById(R.id.row_video_quality).setOnClickListener(v -> showQualityDialog());

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

        tvSelectedQuality.setText(settings.getVideoQuality());
        switchAutoplay.setChecked(settings.isAutoplayEnabled());
        switchNotifications.setChecked(settings.isNotificationsEnabled());

        // Switch change listeners
        switchAutoplay.setOnCheckedChangeListener((buttonView, isChecked) -> 
            settings.setAutoplayEnabled(isChecked));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> 
            settings.setNotificationsEnabled(isChecked));

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
        tvName.setText(profile.getFullName() != null ? profile.getFullName() : profile.getUsername());
        tvEmail.setText(profile.getEmail());
        tvStats.setText("Yêu thích: " + profile.getFavoriteCount() + "   Lịch sử xem: " + profile.getWatchHistoryCount());
        
        SubscriptionDto subscription = profile.getCurrentSubscription();
        tvSubscription.setText(subscription != null
                ? "Gói dịch vụ hiện tại: " + subscription.getPackageName()
                : "Chưa đăng ký gói dịch vụ");
    }

    private void showError() {
        Toast.makeText(this, "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
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

        String currentQuality = settings.getVideoQuality();
        int checkedItem = 0;
        for (int i = 0; i < qualities.length; i++) {
            if (qualities[i].equals(currentQuality)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn chất lượng phát mặc định")
                .setSingleChoiceItems(qualities, checkedItem, (dialog, which) -> {
                    settings.setVideoQuality(qualities[which]);
                    tvSelectedQuality.setText(qualities[which]);
                    dialog.dismiss();
                    Toast.makeText(this, "Đã cập nhật chất lượng mặc định: " + qualities[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
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
                Toast.makeText(this, "Dọn dẹp bộ nhớ đệm thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể dọn dẹp toàn bộ bộ nhớ đệm", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xóa bộ nhớ đệm", Toast.LENGTH_SHORT).show();
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
        textView.setText(
            "CINE-FLOW - ĐIỀU KHOẢN DỊCH VỤ & CHÍNH SÁCH BẢO MẬT\n\n" +
            "Chào mừng bạn đến với ứng dụng xem phim trực tuyến Cine-Flow!\n\n" +
            "1. CHẤP THUẬN ĐIỀU KHOẢN\n" +
            "Bằng việc đăng ký tài khoản và sử dụng các dịch vụ xem phim trên ứng dụng Cine-Flow, bạn hoàn toàn đồng ý và chịu sự ràng buộc bởi các điều khoản sử dụng này.\n\n" +
            "2. BẢN QUYỀN VÀ NỘI DUNG\n" +
            "Toàn bộ phim, trailer, hình ảnh và âm thanh trên ứng dụng thuộc bản quyền của Cine-Flow hoặc các đối tác liên kết hợp pháp. Người dùng nghiêm cấm sao chép, ghi hình, phân phối hoặc thương mại hóa bất kỳ nội dung nào dưới mọi hình thức.\n\n" +
            "3. BẢO MẬT THÔNG TIN CÁ NHÂN\n" +
            "Chúng tôi cam kết bảo mật tuyệt đối các thông tin cá nhân của bạn bao gồm Email, tên đăng nhập, lịch sử xem và các giao dịch gói cước theo chính sách bảo mật của chúng tôi.\n\n" +
            "4. ĐĂNG KÝ VÀ SỬ DỤNG GÓI PREMIUM\n" +
            "Các gói dịch vụ (Cơ bản, Tiêu chuẩn, Cao cấp) sẽ có mức giá và quyền lợi đi kèm được ghi rõ. Việc thanh toán được xử lý bảo mật. Các gói cước đã đăng ký sẽ không được hoàn trả phí trừ trường hợp sự cố phát sinh do hệ thống.\n\n" +
            "Cine-Flow v1.0 - Cảm ơn bạn đã tin tưởng dịch vụ xem phim của chúng tôi!"
        );
        textView.setTextColor(getResources().getColor(R.color.text_primary));
        textView.setTextSize(14f);
        textView.setLineSpacing(4f, 1.1f);
        
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setPadding(48, 32, 48, 32);
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
                .setTitle("Điều khoản & Chính sách bảo mật")
                .setView(scrollView)
                .setPositiveButton("Đồng ý", null)
                .show();
    }

    private void showSupportDialog() {
        final String[] options = {"Gửi Email hỗ trợ (support@cineflow.com)", "Gọi Hotline hỗ trợ (1900 1234)"};
        
        new AlertDialog.Builder(this)
                .setTitle("Liên hệ Hỗ trợ khách hàng")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        sendSupportEmail();
                    } else if (which == 1) {
                        callSupportHotline();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendSupportEmail() {
        android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.setData(android.net.Uri.parse("mailto:support@cineflow.com"));
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Cine-Flow Support Request] - " + tvName.getText().toString());
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Chào đội ngũ hỗ trợ Cine-Flow,\n\nTôi đang gặp sự cố với tài khoản của mình. Dưới đây là thông tin chi tiết:\n...");
        try {
            startActivity(android.content.Intent.createChooser(emailIntent, "Chọn ứng dụng gửi Email"));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng gửi email phù hợp", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSupportHotline() {
        android.content.Intent dialIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
        dialIntent.setData(android.net.Uri.parse("tel:19001234"));
        try {
            startActivity(dialIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }
}
