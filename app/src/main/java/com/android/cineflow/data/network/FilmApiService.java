package com.android.cineflow.data.network;

import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.CreateFilmRequestDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.network.dto.ForgotPasswordRequestDto;
import com.android.cineflow.data.network.dto.FavoriteDto;
import com.android.cineflow.data.network.dto.FootballMatchDto;
import com.android.cineflow.data.network.dto.FootballStandingDto;
import com.android.cineflow.data.network.dto.HomeFilmsDto;
import com.android.cineflow.data.network.dto.LoginRequestDto;
import com.android.cineflow.data.network.dto.LoginResponseDto;
import com.android.cineflow.data.network.dto.PagedResponseDto;
import com.android.cineflow.data.network.dto.PremierLeagueHomeDto;
import com.android.cineflow.data.network.dto.RegisterRequestDto;
import com.android.cineflow.data.network.dto.UpdateFilmRequestDto;
import com.android.cineflow.data.network.dto.UpdateWatchHistoryRequestDto;
import com.android.cineflow.data.network.dto.UpdateProfileRequestDto;
import com.android.cineflow.data.network.dto.ChangePasswordRequestDto;
import com.android.cineflow.data.network.dto.CreateCommentRequestDto;
import com.android.cineflow.data.network.dto.FilmCommentDto;
import com.android.cineflow.data.network.dto.UserAnalyticsDto;
import com.android.cineflow.data.network.dto.UserProfileDto;
import com.android.cineflow.data.network.dto.WatchHistoryDto;
import com.android.cineflow.data.network.dto.FilmDto;
import com.android.cineflow.data.network.dto.ShortsResponseDto;
import com.android.cineflow.data.network.dto.CommentDto;
import com.android.cineflow.data.network.dto.CreateEpisodeRequestDto;
import com.android.cineflow.data.network.dto.EpisodeDto;


import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface FilmApiService {

    // ── Public ──────────────────────────────────────────────────────────────

    @GET("films/home")
    Call<ApiResponseDto<HomeFilmsDto>> getHomeFilms();

    @GET("films/{id}")
    Call<ApiResponseDto<FilmDetailDto>> getFilmById(@Path("id") Integer id);

    @GET("premier-league/home")
    Call<ApiResponseDto<PremierLeagueHomeDto>> getPremierLeagueHome();

    @GET("premier-league/matches")
    Call<ApiResponseDto<List<FootballMatchDto>>> getPremierLeagueMatches(
            @Query("status") String status,
            @Query("date") String date);

    @GET("premier-league/standings")
    Call<ApiResponseDto<List<FootballStandingDto>>> getPremierLeagueStandings();

    @GET("favorites")
    Call<ApiResponseDto<List<FavoriteDto>>> getFavorites();

    @POST("favorites/{filmId}")
    Call<ApiResponseDto<FavoriteDto>> addFavorite(@Path("filmId") Integer filmId);

    @DELETE("favorites/{filmId}")
    Call<ApiResponseDto<Void>> deleteFavorite(@Path("filmId") Integer filmId);

    @GET("watch-history")
    Call<ApiResponseDto<List<WatchHistoryDto>>> getWatchHistory();

    @PUT("watch-history/{episodeId}")
    Call<ApiResponseDto<WatchHistoryDto>> updateWatchHistory(
            @Path("episodeId") Integer episodeId,
            @Body UpdateWatchHistoryRequestDto request);

    @GET("user/me")
    Call<ApiResponseDto<UserProfileDto>> getProfile();

    @GET("user/analytics")
    Call<ApiResponseDto<UserAnalyticsDto>> getUserAnalytics();

    @PUT("user/profile")
    Call<ApiResponseDto<UserProfileDto>> updateProfile(@Body UpdateProfileRequestDto request);

    @POST("user/change-password")
    Call<ApiResponseDto<Void>> changePassword(@Body ChangePasswordRequestDto request);
    
    @GET("films")
    Call<ApiResponseDto<List<FilmDto>>> getFilmsByType(@Query("type") String type);

    @GET("films/shorts")
    Call<ApiResponseDto<ShortsResponseDto>> getShorts();

    @POST("films/shorts/{id}/like")
    Call<ApiResponseDto<Void>> likeShort(@Path("id") String id);

    @DELETE("films/shorts/{id}/like")
    Call<ApiResponseDto<Void>> unlikeShort(@Path("id") String id);

    @GET("films/shorts/{id}/comments")
    Call<ApiResponseDto<List<CommentDto>>> getShortComments(@Path("id") String id);
    ////. ----

    // ── Film comments ───────────────────────────────────────────────────────

    @GET("films/{filmId}/comments")
    Call<ApiResponseDto<List<FilmCommentDto>>> getFilmComments(@Path("filmId") Integer filmId);

    @POST("films/{filmId}/comments")
    Call<ApiResponseDto<FilmCommentDto>> postFilmComment(
            @Path("filmId") Integer filmId,
            @Body CreateCommentRequestDto request);

    @DELETE("films/comments/{commentId}")
    Call<ApiResponseDto<Void>> deleteFilmComment(@Path("commentId") Integer commentId);

    // ── Auth ────────────────────────────────────────────────────────────────

    @POST("auth/login")
    Call<ApiResponseDto<LoginResponseDto>> login(@Body LoginRequestDto request);

    @POST("auth/register")
    Call<ApiResponseDto<Void>> register(@Body RegisterRequestDto request);

    @POST("auth/forgot-password")
    Call<ApiResponseDto<Void>> forgotPassword(@Body ForgotPasswordRequestDto request);

    @POST("auth/refresh-token")
    Call<ApiResponseDto<LoginResponseDto>> refreshToken(@Body com.android.cineflow.data.network.dto.TokenRefreshRequestDto request);

    @POST("auth/logout")
    Call<ApiResponseDto<Void>> logout(@Body com.android.cineflow.data.network.dto.TokenRefreshRequestDto request);

    // ── Admin: Films ────────────────────────────────────────────────────────

    @GET("admin/films")
    Call<ApiResponseDto<PagedResponseDto<FilmDetailDto>>> getAllFilms(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search);

    @GET("admin/films/{id}")
    Call<ApiResponseDto<FilmDetailDto>> getAdminFilmById(@Path("id") Integer id);

    @POST("admin/films")
    Call<ApiResponseDto<FilmDetailDto>> createFilm(@Body CreateFilmRequestDto request);

    @PUT("admin/films/{id}")
    Call<ApiResponseDto<FilmDetailDto>> updateFilm(@Path("id") Integer id, @Body UpdateFilmRequestDto request);

    @DELETE("admin/films/{id}")
    Call<ApiResponseDto<Void>> deleteFilm(@Path("id") Integer id);

    // ── Admin: Episodes ──────────────────────────────────────────────────────

    @POST("admin/films/{filmId}/episodes")
    Call<ApiResponseDto<EpisodeDto>> createEpisode(@Path("filmId") int filmId, @Body CreateEpisodeRequestDto request);

    @PUT("admin/episodes/{id}")
    Call<ApiResponseDto<EpisodeDto>> updateEpisode(@Path("id") int id, @Body CreateEpisodeRequestDto request);

    @DELETE("admin/episodes/{id}")
    Call<ApiResponseDto<Void>> deleteEpisode(@Path("id") int id);

    // ── File Upload ──────────────────────────────────────────────────────────

    @Multipart
    @POST("files/upload")
    Call<ApiResponseDto<String>> uploadFile(@Part MultipartBody.Part file, @Query("folder") String folder);
}

