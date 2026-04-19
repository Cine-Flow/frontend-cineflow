package com.android.cineflow.ui.shorts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.cineflow.data.model.ShortVideo;

import java.util.ArrayList;
import java.util.List;

public class ShortsViewModel extends ViewModel {

    private final MutableLiveData<List<ShortVideo>> _shortVideos = new MutableLiveData<>();
    public LiveData<List<ShortVideo>> shortVideos = _shortVideos;

    public ShortsViewModel() {
        loadShorts();
    }

    private void loadShorts() {
        List<ShortVideo> list = new ArrayList<>();
        // Mock data with a vertical aspect ratio video from public sources
        // Note: For simplicity and since we need a working video URL, we reuse the sample video.
        String sampleVideo = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
        String sampleVideo2 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4";
        String sampleVideo3 = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4";
        
        list.add(new ShortVideo("1", sampleVideo, "Cảnh quay hùng vĩ", "Nguyen Van A", ""));
        list.add(new ShortVideo("2", sampleVideo2, "Khám phá thế giới", "Hau Hoang", ""));
        list.add(new ShortVideo("3", sampleVideo3, "Khoảnh khắc đáng nhớ", "FPT Play", ""));
        list.add(new ShortVideo("4", sampleVideo, "Thiên nhiên tuyệt đẹp", "Cineflow Official", ""));

        _shortVideos.setValue(list);
    }
}
