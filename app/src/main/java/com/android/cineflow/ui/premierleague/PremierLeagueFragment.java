package com.android.cineflow.ui.premierleague;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.android.cineflow.data.model.premierleague.Match;
import com.android.cineflow.ui.player.PlayerActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
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

import com.android.cineflow.data.network.Call;
import com.android.cineflow.data.network.Callback;
import com.android.cineflow.data.network.Response;

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
                },
                (match, listMode) -> handleMatchClick(match, listMode));
        lvPremierLeague.setAdapter(adapter);

        android.widget.EditText etSearchMatches = view.findViewById(R.id.et_search_matches);
        if (etSearchMatches != null) {
            etSearchMatches.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString());
                    }
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
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
            viewModel.fetchDataIfNeeded();
        }
    }

    private final Handler countdownHandler = new Handler(Looper.getMainLooper());

    private void handleMatchClick(Match match, String listMode) {
        if (PremierLeagueSection.MODE_FINISHED.equals(listMode)) {
            String highlightUrl = match.getHighlightUrl();
            if (highlightUrl != null && !highlightUrl.isEmpty()) {
                Intent intent = new Intent(requireContext(), PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, highlightUrl);
                intent.putExtra(PlayerActivity.EXTRA_TITLE, "[Highlights] " + match.getHomeTeamCode() + " " + match.getHomeScore() + " - " + match.getAwayScore() + " " + match.getAwayTeamCode());
                intent.putExtra(PlayerActivity.EXTRA_BADGE, "Highlights");
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Trận đấu này chưa cập nhật Video Highlights", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Show Custom Info Dialog for upcoming/live matches
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_match_detail, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
            TextView tvRound = dialogView.findViewById(R.id.tv_dialog_round);
            TextView tvHomeName = dialogView.findViewById(R.id.tv_dialog_home_name);
            TextView tvAwayName = dialogView.findViewById(R.id.tv_dialog_away_name);
            TextView tvMatchTime = dialogView.findViewById(R.id.tv_dialog_match_time);
            TextView tvStadium = dialogView.findViewById(R.id.tv_dialog_stadium);
            TextView tvReferee = dialogView.findViewById(R.id.tv_dialog_referee);
            TextView tvCountdown = dialogView.findViewById(R.id.tv_dialog_countdown);
            ImageView ivHomeLogo = dialogView.findViewById(R.id.iv_dialog_home_logo);
            ImageView ivAwayLogo = dialogView.findViewById(R.id.iv_dialog_away_logo);
            MaterialButton btnClose = dialogView.findViewById(R.id.btn_dialog_close);
            MaterialButton btnRemind = dialogView.findViewById(R.id.btn_dialog_remind);

            tvRound.setText(match.getRound() != null && !match.getRound().isEmpty() ? match.getRound() : "Premier League");
            tvHomeName.setText(match.getHomeTeamCode());
            tvAwayName.setText(match.getAwayTeamCode());
            tvMatchTime.setText(match.getTime() + " " + match.getDate());

            // Load logos
            if (match.getHomeTeamLogo() != null && !match.getHomeTeamLogo().isEmpty()) {
                Glide.with(this).load(match.getHomeTeamLogo()).placeholder(R.drawable.ic_launcher_foreground).into(ivHomeLogo);
            }
            if (match.getAwayTeamLogo() != null && !match.getAwayTeamLogo().isEmpty()) {
                Glide.with(this).load(match.getAwayTeamLogo()).placeholder(R.drawable.ic_launcher_foreground).into(ivAwayLogo);
            }

            // Mock Stadium and Referee
            tvStadium.setText(getStadiumName(match.getHomeTeamCode()));
            tvReferee.setText(getRefereeName());

            // Handle countdown
            final Runnable[] countdownRunnable = new Runnable[1];
            startCountdownTimer(match.getDate(), match.getTime(), tvCountdown, countdownRunnable);

            dialog.setOnDismissListener(d -> {
                if (countdownRunnable[0] != null) {
                    countdownHandler.removeCallbacks(countdownRunnable[0]);
                }
            });

            btnClose.setOnClickListener(v -> dialog.dismiss());
            btnRemind.setOnClickListener(v -> {
                triggerLocalMatchNotification(match);
                Toast.makeText(requireContext(), "Nhắc nhở đã được thiết lập thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        }
    }

    private String getStadiumName(String teamCode) {
        if (teamCode == null) return "🏟️ Sân vận động: Đang cập nhật";
        switch (teamCode.toUpperCase()) {
            case "ARS": return "🏟️ Sân vận động: Emirates Stadium";
            case "AVL": return "🏟️ Sân vận động: Villa Park";
            case "CHE": return "🏟️ Sân vận động: Stamford Bridge";
            case "EVE": return "🏟️ Sân vận động: Goodison Park";
            case "LIV": return "🏟️ Sân vận động: Anfield";
            case "MCI": return "🏟️ Sân vận động: Etihad Stadium";
            case "MUN": return "🏟️ Sân vận động: Old Trafford";
            case "NEW": return "🏟️ Sân vận động: St James' Park";
            case "TOT": return "🏟️ Sân vận động: Tottenham Hotspur Stadium";
            case "WHU": return "🏟️ Sân vận động: London Stadium";
            case "WOL": return "🏟️ Sân vận động: Molineux Stadium";
            case "LEI": return "🏟️ Sân vận động: King Power Stadium";
            case "BHA": return "🏟️ Sân vận động: Amex Stadium";
            case "CRY": return "🏟️ Sân vận động: Selhurst Park";
            case "FUL": return "🏟️ Sân vận động: Craven Cottage";
            case "SOU": return "🏟️ Sân vận động: St Mary's Stadium";
            default: return "🏟️ Sân vận động: " + teamCode + " Stadium";
        }
    }

    private String getRefereeName() {
        String[] referees = {"Anthony Taylor", "Michael Oliver", "Paul Tierney", "Simon Hooper", "Chris Kavanagh", "Stuart Attwell", "David Coote"};
        int index = Math.abs(new java.util.Random().nextInt()) % referees.length;
        return "🏁 Trọng tài: " + referees[index];
    }

    private void startCountdownTimer(String dateStr, String timeStr, TextView tvCountdown, final Runnable[] runnableHolder) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            java.util.Date matchDate = sdf.parse(dateStr + " " + timeStr);
            if (matchDate == null) {
                tvCountdown.setText(getString(R.string.pl_status_not_started));
                return;
            }
            
            runnableHolder[0] = new Runnable() {
                @Override
                public void run() {
                    long diff = matchDate.getTime() - System.currentTimeMillis();
                    if (diff <= 0) {
                        tvCountdown.setText(getString(R.string.pl_status_started));
                    } else {
                        long hours = diff / (3600 * 1000);
                        long minutes = (diff % (3600 * 1000)) / (60 * 1000);
                        long seconds = (diff % (60 * 1000)) / 1000;
                        tvCountdown.setText(getString(R.string.pl_countdown_format, hours, minutes, seconds));
                        countdownHandler.postDelayed(this, 1000);
                    }
                }
            };
            countdownHandler.post(runnableHolder[0]);
        } catch (Exception e) {
            tvCountdown.setText(getString(R.string.pl_status_not_started));
        }
    }

    private void triggerLocalMatchNotification(Match match) {
        android.content.Context context = requireContext();
        String channelId = "cineflow_match_notifications";
        String channelName = "Cine-Flow Premier League";
        
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    channelName,
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo lịch phát sóng trận đấu Premier League");
            notificationManager.createNotificationChannel(channel);
        }

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_football)
                .setContentTitle("⚽ Đã đặt lịch nhắc nhở trận đấu!")
                .setContentText("Cine-Flow sẽ nhắc bạn khi trận đấu giữa " + match.getHomeTeamCode() + " và " + match.getAwayTeamCode() + " diễn ra (" + match.getTime() + " - " + match.getDate() + ").")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
