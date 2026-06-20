package com.android.cineflow.data.network;

import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.ChangePasswordRequestDto;
import com.android.cineflow.data.network.dto.CommentDto;
import com.android.cineflow.data.network.dto.CreateCommentRequestDto;
import com.android.cineflow.data.network.dto.CreateFilmRequestDto;
import com.android.cineflow.data.network.dto.FavoriteDto;
import com.android.cineflow.data.network.dto.FilmCommentDto;
import com.android.cineflow.data.network.dto.FilmDetailDto;
import com.android.cineflow.data.network.dto.FilmDto;
import com.android.cineflow.data.network.dto.FootballMatchDto;
import com.android.cineflow.data.network.dto.FootballStandingDto;
import com.android.cineflow.data.network.dto.ForgotPasswordRequestDto;
import com.android.cineflow.data.network.dto.HomeFilmsDto;
import com.android.cineflow.data.network.dto.LoginRequestDto;
import com.android.cineflow.data.network.dto.LoginResponseDto;
import com.android.cineflow.data.network.dto.PagedResponseDto;
import com.android.cineflow.data.network.dto.PremierLeagueHomeDto;
import com.android.cineflow.data.network.dto.RegisterRequestDto;
import com.android.cineflow.data.network.dto.ShortsResponseDto;
import com.android.cineflow.data.network.dto.TokenRefreshRequestDto;
import com.android.cineflow.data.network.dto.UpdateFilmRequestDto;
import com.android.cineflow.data.network.dto.UpdateProfileRequestDto;
import com.android.cineflow.data.network.dto.UpdateWatchHistoryRequestDto;
import com.android.cineflow.data.network.dto.UserAnalyticsDto;
import com.android.cineflow.data.network.dto.UserProfileDto;
import com.android.cineflow.data.network.dto.WatchHistoryDto;

import java.util.List;

public interface FilmApiService {

    Call<ApiResponseDto<HomeFilmsDto>> getHomeFilms();

    Call<ApiResponseDto<FilmDetailDto>> getFilmById(Integer id);

    Call<ApiResponseDto<PremierLeagueHomeDto>> getPremierLeagueHome();

    Call<ApiResponseDto<List<FootballMatchDto>>> getPremierLeagueMatches(String status, String date);

    Call<ApiResponseDto<List<FootballStandingDto>>> getPremierLeagueStandings();

    Call<ApiResponseDto<List<FavoriteDto>>> getFavorites();

    Call<ApiResponseDto<FavoriteDto>> addFavorite(Integer filmId);

    Call<ApiResponseDto<Void>> deleteFavorite(Integer filmId);

    Call<ApiResponseDto<List<WatchHistoryDto>>> getWatchHistory();

    Call<ApiResponseDto<WatchHistoryDto>> updateWatchHistory(Integer episodeId, UpdateWatchHistoryRequestDto request);

    Call<ApiResponseDto<UserProfileDto>> getProfile();

    Call<ApiResponseDto<UserAnalyticsDto>> getUserAnalytics();

    Call<ApiResponseDto<UserProfileDto>> updateProfile(UpdateProfileRequestDto request);

    Call<ApiResponseDto<Void>> changePassword(ChangePasswordRequestDto request);
    
    Call<ApiResponseDto<List<FilmDto>>> getFilmsByType(String type);

    Call<ApiResponseDto<ShortsResponseDto>> getShorts();

    Call<ApiResponseDto<Void>> likeShort(String id);

    Call<ApiResponseDto<Void>> unlikeShort(String id);

    Call<ApiResponseDto<List<CommentDto>>> getShortComments(String id);

    Call<ApiResponseDto<CommentDto>> postShortComment(String id, CreateCommentRequestDto request);

    Call<ApiResponseDto<List<FilmCommentDto>>> getFilmComments(Integer filmId);


    Call<ApiResponseDto<FilmCommentDto>> postFilmComment(Integer filmId, CreateCommentRequestDto request);

    Call<ApiResponseDto<Void>> deleteFilmComment(Integer commentId);

    Call<ApiResponseDto<LoginResponseDto>> login(LoginRequestDto request);

    Call<ApiResponseDto<Void>> register(RegisterRequestDto request);

    Call<ApiResponseDto<Void>> forgotPassword(ForgotPasswordRequestDto request);

    Call<ApiResponseDto<LoginResponseDto>> refreshToken(TokenRefreshRequestDto request);

    Call<ApiResponseDto<Void>> logout(TokenRefreshRequestDto request);

    Call<ApiResponseDto<PagedResponseDto<FilmDetailDto>>> getAllFilms(int page, int size, String search);

    Call<ApiResponseDto<FilmDetailDto>> getAdminFilmById(Integer id);

    Call<ApiResponseDto<FilmDetailDto>> createFilm(CreateFilmRequestDto request);

    Call<ApiResponseDto<FilmDetailDto>> updateFilm(Integer id, UpdateFilmRequestDto request);

    Call<ApiResponseDto<Void>> deleteFilm(Integer id);
}
