package com.android.cineflow.data.network;

import android.net.Uri;

import com.android.cineflow.data.network.dto.ApiResponseDto;
import com.android.cineflow.data.network.dto.AdminAnalyticsDto;
import com.android.cineflow.data.network.dto.AdminCategoryDto;
import com.android.cineflow.data.network.dto.AdminCategoryRequestDto;
import com.android.cineflow.data.network.dto.AdminUserDto;
import com.android.cineflow.data.network.dto.AdminUserRequestDto;
import com.android.cineflow.data.network.dto.ChangePasswordRequestDto;
import com.android.cineflow.data.network.dto.CommentDto;
import com.android.cineflow.data.network.dto.CreateCommentRequestDto;
import com.android.cineflow.data.network.dto.CreateEpisodeRequestDto;
import com.android.cineflow.data.network.dto.CreateFilmRequestDto;
import com.android.cineflow.data.network.dto.EpisodeDto;
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
import com.android.cineflow.data.network.dto.ResetPasswordRequestDto;
import com.android.cineflow.data.network.dto.ShortsResponseDto;
import com.android.cineflow.data.network.dto.TokenRefreshRequestDto;
import com.android.cineflow.data.network.dto.UpdateFilmRequestDto;
import com.android.cineflow.data.network.dto.UpdateProfileRequestDto;
import com.android.cineflow.data.network.dto.UpdateWatchHistoryRequestDto;
import com.android.cineflow.data.network.dto.UserAnalyticsDto;
import com.android.cineflow.data.network.dto.UserProfileDto;
import com.android.cineflow.data.network.dto.WatchHistoryDto;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;

public class VolleyFilmApiServiceImpl implements FilmApiService {
    private final RequestQueue requestQueue;
    private final String baseUrl;
    private final OkHttpClient okHttpClient;
    private final Gson gson = new Gson();

    public VolleyFilmApiServiceImpl(RequestQueue requestQueue, String baseUrl, OkHttpClient okHttpClient) {
        this.requestQueue = requestQueue;
        this.baseUrl = baseUrl;
        this.okHttpClient = okHttpClient;
    }

    private <T> Call<T> createCall(String path, int method, Object body, Type responseType) {
        String url = baseUrl + path;
        String bodyJson = body != null ? gson.toJson(body) : null;
        return new VolleyCall<>(url, method, bodyJson, responseType, requestQueue);
    }

    @Override
    public Call<ApiResponseDto<HomeFilmsDto>> getHomeFilms() {
        return createCall("films/home", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<HomeFilmsDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<FilmDetailDto>> getFilmById(Integer id) {
        return createCall("films/" + id, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<FilmDetailDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<PremierLeagueHomeDto>> getPremierLeagueHome() {
        return createCall("premier-league/home", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<PremierLeagueHomeDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<FootballMatchDto>>> getPremierLeagueMatches(String status, String date) {
        String query = "";
        if (status != null) {
            query += "status=" + Uri.encode(status);
        }
        if (date != null) {
            if (!query.isEmpty()) query += "&";
            query += "date=" + Uri.encode(date);
        }
        String path = "premier-league/matches" + (query.isEmpty() ? "" : "?" + query);
        return createCall(path, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<FootballMatchDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<FootballStandingDto>>> getPremierLeagueStandings() {
        return createCall("premier-league/standings", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<FootballStandingDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<FavoriteDto>>> getFavorites() {
        return createCall("favorites", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<FavoriteDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<FavoriteDto>> addFavorite(Integer filmId) {
        return createCall("favorites/" + filmId, Request.Method.POST, null,
                new TypeToken<ApiResponseDto<FavoriteDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> deleteFavorite(Integer filmId) {
        return createCall("favorites/" + filmId, Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<WatchHistoryDto>>> getWatchHistory() {
        return createCall("watch-history", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<WatchHistoryDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<WatchHistoryDto>> updateWatchHistory(Integer episodeId, UpdateWatchHistoryRequestDto request) {
        return createCall("watch-history/" + episodeId, Request.Method.PUT, request,
                new TypeToken<ApiResponseDto<WatchHistoryDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<UserProfileDto>> getProfile() {
        return createCall("user/me", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<UserProfileDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<UserAnalyticsDto>> getUserAnalytics() {
        return createCall("user/analytics", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<UserAnalyticsDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<UserProfileDto>> updateProfile(UpdateProfileRequestDto request) {
        return createCall("user/profile", Request.Method.PUT, request,
                new TypeToken<ApiResponseDto<UserProfileDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> changePassword(ChangePasswordRequestDto request) {
        return createCall("user/change-password", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<FilmDto>>> getFilmsByType(String type) {
        String path = "films" + (type != null ? "?type=" + Uri.encode(type) : "");
        return createCall(path, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<FilmDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<ShortsResponseDto>> getShorts() {
        return createCall("films/shorts", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<ShortsResponseDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> likeShort(String id) {
        return createCall("films/shorts/" + id + "/like", Request.Method.POST, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> unlikeShort(String id) {
        return createCall("films/shorts/" + id + "/like", Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<CommentDto>>> getShortComments(String id) {
        return createCall("films/shorts/" + id + "/comments", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<CommentDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<CommentDto>> postShortComment(String id, CreateCommentRequestDto request) {
        return createCall("films/shorts/" + id + "/comments", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<CommentDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<List<FilmCommentDto>>> getFilmComments(Integer filmId) {

        return createCall("films/" + filmId + "/comments", Request.Method.GET, null,
                new TypeToken<ApiResponseDto<List<FilmCommentDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<FilmCommentDto>> postFilmComment(Integer filmId, CreateCommentRequestDto request) {
        return createCall("films/" + filmId + "/comments", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<FilmCommentDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> deleteFilmComment(Integer commentId) {
        return createCall("films/comments/" + commentId, Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<LoginResponseDto>> login(LoginRequestDto request) {
        return createCall("auth/login", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<LoginResponseDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> register(RegisterRequestDto request) {
        return createCall("auth/register", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> forgotPassword(ForgotPasswordRequestDto request) {
        return createCall("auth/forgot-password", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> resetPassword(ResetPasswordRequestDto request) {
        return createCall("auth/reset-password", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Boolean>> validateResetToken(String token) {
        return createCall("auth/validate-reset-token?token=" + Uri.encode(token), Request.Method.GET, null,
                new TypeToken<ApiResponseDto<Boolean>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<LoginResponseDto>> refreshToken(TokenRefreshRequestDto request) {
        return createCall("auth/refresh-token", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<LoginResponseDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> logout(TokenRefreshRequestDto request) {
        return createCall("auth/logout", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<PagedResponseDto<FilmDetailDto>>> getAllFilms(int page, int size, String search) {
        String query = "page=" + page + "&size=" + size;
        if (search != null) {
            query += "&search=" + Uri.encode(search);
        }
        return createCall("admin/films?" + query, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<PagedResponseDto<FilmDetailDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<PagedResponseDto<AdminCategoryDto>>> getAdminCategories(int page, int size, String search) {
        String query = "page=" + page + "&size=" + size;
        if (search != null && !search.trim().isEmpty()) {
            query += "&search=" + Uri.encode(search.trim());
        }
        return createCall("admin/categories?" + query, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<PagedResponseDto<AdminCategoryDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<AdminCategoryDto>> createCategory(AdminCategoryRequestDto request) {
        return createCall("admin/categories", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<AdminCategoryDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<AdminCategoryDto>> updateCategory(Integer id, AdminCategoryRequestDto request) {
        return createCall("admin/categories/" + id, Request.Method.PUT, request,
                new TypeToken<ApiResponseDto<AdminCategoryDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> deleteCategory(Integer id) {
        return createCall("admin/categories/" + id, Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<PagedResponseDto<AdminUserDto>>> getAdminUsers(int page, int size, String search) {
        String query = "page=" + page + "&size=" + size;
        if (search != null && !search.trim().isEmpty()) {
            query += "&search=" + Uri.encode(search.trim());
        }
        return createCall("admin/users?" + query, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<PagedResponseDto<AdminUserDto>>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<AdminUserDto>> updateUser(String id, AdminUserRequestDto request) {
        return createCall("admin/users/" + Uri.encode(id), Request.Method.PUT, request,
                new TypeToken<ApiResponseDto<AdminUserDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> deleteUser(String id) {
        return createCall("admin/users/" + Uri.encode(id), Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> resetUserPassword(String id) {
        return createCall("admin/users/" + Uri.encode(id) + "/reset-password", Request.Method.POST, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<AdminUserDto>> setUserStatus(String id, boolean enabled) {
        return createCall("admin/users/" + Uri.encode(id) + "/status?enabled=" + enabled, Request.Method.PUT, null,
                new TypeToken<ApiResponseDto<AdminUserDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<AdminAnalyticsDto>> getAdminAnalytics(int period) {
        return createCall("admin/analytics?period=" + period, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<AdminAnalyticsDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<FilmDetailDto>> getAdminFilmById(Integer id) {
        return createCall("admin/films/" + id, Request.Method.GET, null,
                new TypeToken<ApiResponseDto<FilmDetailDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<FilmDetailDto>> createFilm(CreateFilmRequestDto request) {
        return createCall("admin/films", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<FilmDetailDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<FilmDetailDto>> updateFilm(Integer id, UpdateFilmRequestDto request) {
        return createCall("admin/films/" + id, Request.Method.PUT, request,
                new TypeToken<ApiResponseDto<FilmDetailDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> deleteFilm(Integer id) {
        return createCall("admin/films/" + id, Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<EpisodeDto>> createEpisode(int filmId, CreateEpisodeRequestDto request) {
        return createCall("admin/films/" + filmId + "/episodes", Request.Method.POST, request,
                new TypeToken<ApiResponseDto<EpisodeDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<EpisodeDto>> updateEpisode(int episodeId, CreateEpisodeRequestDto request) {
        return createCall("admin/episodes/" + episodeId, Request.Method.PUT, request,
                new TypeToken<ApiResponseDto<EpisodeDto>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<Void>> deleteEpisode(int episodeId) {
        return createCall("admin/episodes/" + episodeId, Request.Method.DELETE, null,
                new TypeToken<ApiResponseDto<Void>>(){}.getType());
    }

    @Override
    public Call<ApiResponseDto<String>> uploadFile(MultipartBody.Part file, String folder) {
        String path = "files/upload";
        if (folder != null && !folder.isEmpty()) {
            path += "?folder=" + Uri.encode(folder);
        }
        return new OkHttpUploadCall<>(okHttpClient, baseUrl + path, file,
                new TypeToken<ApiResponseDto<String>>(){}.getType());
    }
}
