package com.android.cineflow.data.network;

import okhttp3.ResponseBody;

public class Response<T> {
    private final T body;
    private final int code;
    private final boolean isSuccessful;
    private final ResponseBody errorBody;

    private Response(T body, int code, boolean isSuccessful, ResponseBody errorBody) {
        this.body = body;
        this.code = code;
        this.isSuccessful = isSuccessful;
        this.errorBody = errorBody;
    }

    public static <T> Response<T> success(T body) {
        return new Response<>(body, 200, true, null);
    }

    public static <T> Response<T> error(int code, ResponseBody errorBody) {
        return new Response<>(null, code, false, errorBody);
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public T body() {
        return body;
    }

    public int code() {
        return code;
    }

    public ResponseBody errorBody() {
        return errorBody;
    }
}
