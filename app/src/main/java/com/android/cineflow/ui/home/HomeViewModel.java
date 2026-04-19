package com.android.cineflow.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.data.common.uimodel.HomeMapper;
import com.android.cineflow.data.common.uimodel.HomeSection;
import com.android.cineflow.data.model.Movie;
import com.android.cineflow.data.model.SportEvent;
import com.android.cineflow.data.repository.HomeRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final HomeRepository repository = HomeRepository.getInstance();

    // The single stream HomeFragment observes
    private final MediatorLiveData<List<HomeSection>> sections = new MediatorLiveData<>();

    public HomeViewModel() {
        loadSections();
    }

    public LiveData<List<HomeSection>> getSections() {
        return sections;
    }

    // ── Private assembly ─────────────────────────────────────────────────────

    private void loadSections() {
        // Observe all repo streams; rebuild sections whenever any updates
        sections.addSource(repository.getBannerMovies(),      movies  -> rebuild());
        sections.addSource(repository.getNewReleases(),       movies  -> rebuild());
        sections.addSource(repository.getSportEvents(),       events  -> rebuild());
        sections.addSource(repository.getHotSeries(),         movies  -> rebuild());
        sections.addSource(repository.getDailyMovies(),       movies  -> rebuild());
    }

    /**
     * Assembles the full ordered list of sections.
     * Order here = order on screen.
     */
    private void rebuild() {
        List<HomeSection> result = new ArrayList<>();

        List<Movie> banners     = getValue(repository.getBannerMovies());
        List<Movie> newReleases = getValue(repository.getNewReleases());
        List<SportEvent> sports = getValue(repository.getSportEvents());
        List<Movie> hotSeries   = getValue(repository.getHotSeries());
        List<Movie> daily       = getValue(repository.getDailyMovies());

        if (!banners.isEmpty()) {
            result.add(new HomeSection(
                    HomeSection.TYPE_BANNER, null, null,
                    HomeMapper.fromMovies(banners, ContentCard.STYLE_BANNER)));
        }
        if (!newReleases.isEmpty()) {
            result.add(new HomeSection(
                    HomeSection.TYPE_SECTION_ROW, "Mới ra mắt", null,
                    HomeMapper.fromMovies(newReleases, ContentCard.STYLE_PORTRAIT)));
        }
        if (!sports.isEmpty()) {
            result.add(new HomeSection(
                    HomeSection.TYPE_SPORT_ROW, "Sự kiện Thể thao", null,
                    HomeMapper.fromSportEvents(sports)));
        }
        if (!hotSeries.isEmpty()) {
            result.add(new HomeSection(
                    HomeSection.TYPE_SECTION_ROW, "Phim bộ hot", "Xem thêm",
                    HomeMapper.fromMovies(hotSeries, ContentCard.STYLE_PORTRAIT)));
        }
        if (!daily.isEmpty()) {
            result.add(new HomeSection(
                    HomeSection.TYPE_SECTION_ROW, "Cày phim hay mỗi ngày", "Xem thêm",
                    HomeMapper.fromMovies(daily, ContentCard.STYLE_PORTRAIT)));
        }

        sections.setValue(result);
    }

    /** Safe getter — returns empty list if LiveData has no value yet */
    private <T> List<T> getValue(LiveData<List<T>> liveData) {
        List<T> val = liveData.getValue();
        return val != null ? val : new ArrayList<>();
    }
}
