package com.android.cineflow.ui.catalog;

public class MoviesFragment extends FilmCatalogFragment {
    @Override
    protected String getFilmType() {
        return "SINGLE";
    }

    @Override
    protected String getCatalogTitle() {
        return "Phim lẻ";
    }
}
