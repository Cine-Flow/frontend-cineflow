package com.android.cineflow.ui.premierleague;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.android.cineflow.data.repository.PremierLeagueRepository;
import java.util.List;

public class PremierLeagueViewModel extends ViewModel {
    private final PremierLeagueRepository repository = PremierLeagueRepository.getInstance();

    public LiveData<List<PremierLeagueSection>> getSections() {
        return repository.getSections();
    }

    public LiveData<Boolean> getLoading() {
        return repository.getLoading();
    }

    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }

    public void refresh() {
        repository.fetchData();
    }

    public void fetchDataIfNeeded() {
        repository.fetchDataIfNeeded();
    }

    public void expandSection(String mode) {
        repository.expandSection(mode);
    }

    public void loadFixturesForDate(String apiDate, String displayDate) {
        repository.loadFixturesForDate(apiDate, displayDate);
    }

    public void loadMatchesForDate(String mode, String apiDate, String displayDate) {
        repository.loadMatchesForDate(mode, apiDate, displayDate);
    }
}
