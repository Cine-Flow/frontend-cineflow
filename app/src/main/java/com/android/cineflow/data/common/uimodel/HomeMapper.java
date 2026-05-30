package com.android.cineflow.data.common.uimodel;

import com.android.cineflow.data.model.Movie;
import com.android.cineflow.data.model.SportEvent;

import java.util.ArrayList;
import java.util.List;

public class HomeMapper {

    // Prevent instantiation — all methods are static utilities
    private HomeMapper() {}

    public static ContentCard fromMovie(Movie movie, int cardStyle) {
        String badge = movie.is4K() ? "4K" : (movie.isNew() ? "NEW" : "");
        return new ContentCard(
                movie.getId(),
                movie.getTitle(),
                movie.getThumbnailUrl(),
                badge,
                cardStyle,
                null,
                movie.getGenre() // This is mapped from film's type
        );
    }

    public static ContentCard fromSportEvent(SportEvent event) {
        String badge = event.isLive() ? "LIVE" : "";
        return new ContentCard(
                event.getId(),
                event.getTitle(),
                event.getThumbnailUrl(),
                badge,
                ContentCard.STYLE_LANDSCAPE,
                event.getStreamUrl(),
                "LIVE"
        );
    }

    public static List<ContentCard> fromMovies(List<Movie> movies, int cardStyle) {
        List<ContentCard> cards = new ArrayList<>();
        for (Movie m : movies) cards.add(fromMovie(m, cardStyle));
        return cards;
    }

    public static List<ContentCard> fromSportEvents(List<SportEvent> events) {
        List<ContentCard> cards = new ArrayList<>();
        for (SportEvent e : events) cards.add(fromSportEvent(e));
        return cards;
    }
}
