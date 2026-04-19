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
}
