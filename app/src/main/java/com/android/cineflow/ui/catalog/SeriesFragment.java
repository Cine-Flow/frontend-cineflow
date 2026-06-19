package com.android.cineflow.ui.catalog;

public class SeriesFragment extends FilmCatalogFragment {
    @Override
    protected String getFilmType() {
        return "SERIES";
    }

    @Override
    protected String getCatalogTitle() {
        return "Phim bộ";
    }
}
