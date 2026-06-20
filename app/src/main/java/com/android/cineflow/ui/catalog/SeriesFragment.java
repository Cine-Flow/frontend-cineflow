package com.android.cineflow.ui.catalog;

public class SeriesFragment extends FilmCatalogFragment {
    @Override
    protected String getFilmType() {
        return "SERIES";
    }

    @Override
    protected int getCatalogTitleResId() {
        return com.android.cineflow.R.string.nav_series;
    }
}
