package com.android.cineflow.data.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private final SharedPreferences prefs;
    private final Gson gson;
    private final Handler mainHandler;

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
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_DOWNLOADS, json).apply();
    }

    public boolean isEpisodeDownloaded(String filmId, int episodeIndex) {
        if (filmId == null) return false;
        List<OfflineEpisode> list = getDownloadedEpisodes();
        for (OfflineEpisode ep : list) {
            if (filmId.equals(ep.filmId) && ep.episodeIndex == episodeIndex) {
                return true;
            }
        }
        return false;
    }

    public int getDownloadedCount() {
        return getDownloadedEpisodes().size();
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
            list.remove(toRemove);
            saveDownloadedEpisodes(list);
        }
    }

    public synchronized void clearAllDownloads() {
        prefs.edit().remove(KEY_DOWNLOADS).apply();
    }

    /**
     * Simulates downloading an episode asynchronously over 3 seconds,
     * updating progress listener periodically.
     */
    public void startDownloadEpisode(
            final String filmId, final String filmTitle, final String filmCoverUrl,
            final int episodeIndex, final String episodeTitle, final String duration,
            final OnDownloadProgressListener listener) {

        if (isEpisodeDownloaded(filmId, episodeIndex)) {
            if (listener != null) {
                listener.onError("Tập phim này đã được tải xuống trước đó.");
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate progress increments of 10%
                    for (int p = 0; p <= 100; p += 10) {
                        final int progress = p;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) listener.onProgress(progress);
                            }
                        });
                        Thread.sleep(300); // 3 seconds total download time
                    }

                    // Download complete, save offline episode
                    String size = (80 + (int)(Math.random() * 60)) + " MB"; // Mock size
                    String downloadDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    // A mock local source path or a premium static stream url that works offline
                    String localPath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";

                    final OfflineEpisode episode = new OfflineEpisode(
                            filmId, filmTitle, filmCoverUrl, episodeIndex,
                            episodeTitle, duration, size, downloadDate, localPath
                    );

                    synchronized (OfflineDownloadManager.this) {
                        List<OfflineEpisode> list = getDownloadedEpisodes();
                        list.add(episode);
                        saveDownloadedEpisodes(list);
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) listener.onComplete(episode);
                        }
                    });

                } catch (InterruptedException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) listener.onError("Quá trình tải xuống bị gián đoạn.");
                        }
                    });
                }
            }
        }).start();
    }
}
