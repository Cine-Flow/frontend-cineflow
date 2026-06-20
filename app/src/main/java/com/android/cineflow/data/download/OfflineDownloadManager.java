package com.android.cineflow.data.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OfflineDownloadManager {

    public interface OnDownloadProgressListener {
        void onProgress(int progress);
        void onComplete(OfflineEpisode episode);
        void onError(String error);
    }

    public static class OfflineEpisode {
        public String filmId;
        public String filmTitle;
        public String filmCoverUrl;
        public int episodeIndex;
        public String episodeTitle;
        public String duration;
        public String size;
        public String downloadDate;
        public String localPath;

        public OfflineEpisode() {}

        public OfflineEpisode(String filmId, String filmTitle, String filmCoverUrl,
                              int episodeIndex, String episodeTitle, String duration,
                              String size, String downloadDate, String localPath) {
            this.filmId = filmId;
            this.filmTitle = filmTitle;
            this.filmCoverUrl = filmCoverUrl;
            this.episodeIndex = episodeIndex;
            this.episodeTitle = episodeTitle;
            this.duration = duration;
            this.size = size;
            this.downloadDate = downloadDate;
            this.localPath = localPath;
        }
    }

    private static final String PREFS_NAME = "cineflow_offline_downloads";
    private static final String KEY_DOWNLOADS = "downloads_list";
    private static volatile OfflineDownloadManager instance;

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private final Handler mainHandler;
    private final OkHttpClient httpClient = new OkHttpClient();

    public static void init(Context context) {
        if (instance == null) {
            synchronized (OfflineDownloadManager.class) {
                if (instance == null) {
                    instance = new OfflineDownloadManager(context.getApplicationContext());
                }
            }
        }
    }

    public static OfflineDownloadManager getInstance() {
        return instance;
    }

    private OfflineDownloadManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized List<OfflineEpisode> getDownloadedEpisodes() {
        String json = prefs.getString(KEY_DOWNLOADS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<ArrayList<OfflineEpisode>>() {}.getType();
            List<OfflineEpisode> list = gson.fromJson(json, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private synchronized void saveDownloadedEpisodes(List<OfflineEpisode> list) {
        prefs.edit().putString(KEY_DOWNLOADS, gson.toJson(list)).apply();
    }

    public boolean isEpisodeDownloaded(String filmId, int episodeIndex) {
        if (filmId == null) return false;
        for (OfflineEpisode ep : getDownloadedEpisodes()) {
            if (filmId.equals(ep.filmId) && ep.episodeIndex == episodeIndex && fileExists(ep.localPath)) {
                return true;
            }
        }
        return false;
    }

    public int getDownloadedCount() {
        int count = 0;
        for (OfflineEpisode ep : getDownloadedEpisodes()) {
            if (fileExists(ep.localPath)) count++;
        }
        return count;
    }

    public synchronized void deleteDownloadedEpisode(String filmId, int episodeIndex) {
        if (filmId == null) return;
        List<OfflineEpisode> list = getDownloadedEpisodes();
        OfflineEpisode toRemove = null;
        for (OfflineEpisode ep : list) {
            if (filmId.equals(ep.filmId) && ep.episodeIndex == episodeIndex) {
                toRemove = ep;
                break;
            }
        }
        if (toRemove != null) {
            deleteLocalFile(toRemove.localPath);
            list.remove(toRemove);
            saveDownloadedEpisodes(list);
        }
    }

    public synchronized void clearAllDownloads() {
        for (OfflineEpisode ep : getDownloadedEpisodes()) {
            deleteLocalFile(ep.localPath);
        }
        prefs.edit().remove(KEY_DOWNLOADS).apply();
    }

    public void startDownloadEpisode(
            final String filmId, final String filmTitle, final String filmCoverUrl,
            final int episodeIndex, final String episodeTitle, final String duration,
            final String videoUrl, final OnDownloadProgressListener listener) {

        if (isEpisodeDownloaded(filmId, episodeIndex)) {
            postError(listener, "Tap phim nay da duoc tai xuong truoc do.");
            return;
        }
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            postError(listener, "Tap phim chua co duong dan video de tai xuong.");
            return;
        }

        new Thread(() -> {
            File outFile = buildOutputFile(filmId, episodeIndex, videoUrl);
            try {
                Request request = new Request.Builder().url(resolveLocalhostUrl(videoUrl)).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IllegalStateException("HTTP " + response.code());
                    }
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new IllegalStateException("Empty response body");
                    }
                    long total = body.contentLength();
                    long downloaded = 0;
                    byte[] buffer = new byte[16 * 1024];
                    try (InputStream in = body.byteStream();
                         FileOutputStream out = new FileOutputStream(outFile)) {
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            downloaded += read;
                            if (total > 0) {
                                int progress = Math.min(100, (int) ((downloaded * 100) / total));
                                mainHandler.post(() -> {
                                    if (listener != null) listener.onProgress(progress);
                                });
                            }
                        }
                    }
                }

                long fileSize = outFile.length();
                String size = formatSize(fileSize);
                String downloadDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                OfflineEpisode episode = new OfflineEpisode(
                        filmId, filmTitle, filmCoverUrl, episodeIndex, episodeTitle,
                        duration, size, downloadDate, Uri.fromFile(outFile).toString());

                synchronized (OfflineDownloadManager.this) {
                    List<OfflineEpisode> list = getDownloadedEpisodes();
                    list.add(episode);
                    saveDownloadedEpisodes(list);
                }

                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onProgress(100);
                        listener.onComplete(episode);
                    }
                });
            } catch (Exception e) {
                deleteFile(outFile);
                postError(listener, "Khong the tai xuong: " + e.getMessage());
            }
        }).start();
    }

    private File buildOutputFile(String filmId, int episodeIndex, String videoUrl) {
        File dir = new File(context.getExternalFilesDir(null), "offline_videos");
        if (!dir.exists()) dir.mkdirs();
        String cleanFilmId = filmId == null ? "film" : filmId.replaceAll("[^A-Za-z0-9_-]", "_");
        String extension = ".mp4";
        int dot = videoUrl.lastIndexOf('.');
        if (dot > -1 && dot < videoUrl.length() - 1) {
            String candidate = videoUrl.substring(dot).split("\\?")[0];
            if (candidate.matches("\\.[A-Za-z0-9]{2,5}")) extension = candidate;
        }
        return new File(dir, cleanFilmId + "_ep_" + episodeIndex + extension);
    }

    private String resolveLocalhostUrl(String url) {
        if (url != null && url.contains("localhost:9000")) {
            return url.replace("localhost:9000", "10.0.2.2:9000");
        }
        return url;
    }

    private String formatSize(long bytes) {
        double mb = bytes / (1024d * 1024d);
        return String.format(Locale.getDefault(), "%.1f MB", mb);
    }

    private boolean fileExists(String localPath) {
        if (localPath == null || localPath.isEmpty()) return false;
        try {
            return new File(Uri.parse(localPath).getPath()).exists();
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteLocalFile(String localPath) {
        if (localPath == null || localPath.isEmpty()) return;
        try {
            deleteFile(new File(Uri.parse(localPath).getPath()));
        } catch (Exception ignored) {}
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void postError(OnDownloadProgressListener listener, String error) {
        mainHandler.post(() -> {
            if (listener != null) listener.onError(error);
        });
    }
}
