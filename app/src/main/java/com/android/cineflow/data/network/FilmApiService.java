package com.android.cineflow.data.network;

import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.HomeFilmsDto;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FilmApiService {

    @GET("films/home")
    Call<ApiResponseDto<HomeFilmsDto>> getHomeFilms();

    @GET("films/{id}")
    Call<ApiResponseDto<com.android.cineflow.data.network.dto.FilmDetailDto>> getFilmById(@retrofit2.http.Path("id") Integer id);
}
