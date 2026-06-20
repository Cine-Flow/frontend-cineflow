package com.android.cineflow.ui.catalog;

public class MoviesFragment extends FilmCatalogFragment {
    @Override
    protected String getFilmType() {
        return "SINGLE";
    }

    @Override
    protected int getCatalogTitleResId() {
        return com.android.cineflow.R.string.nav_movies;
    }
}
