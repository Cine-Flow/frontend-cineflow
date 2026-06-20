package com.android.cineflow.data.network;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class OkHttpUploadCall<T> implements Call<T> {
    private final OkHttpClient client;
    private final String url;
    private final MultipartBody.Part filePart;
    private final Type responseType;
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean executed = false;
    private boolean canceled = false;
    private okhttp3.Call pendingCall;

    public OkHttpUploadCall(OkHttpClient client, String url, MultipartBody.Part filePart, Type responseType) {
        this.client = client;
        this.url = url;
        this.filePart = filePart;
        this.responseType = responseType;
    }

    @Override
    public Response<T> execute() throws IOException {
        markExecuted();
        if (canceled) {
            throw new IOException("Canceled");
        }

        okhttp3.Call call = newCall();
        synchronized (this) {
            pendingCall = call;
        }

        try (okhttp3.Response rawResponse = call.execute()) {
            return toResponse(rawResponse);
        }
    }

    @Override
    public void enqueue(Callback<T> callback) {
        markExecuted();
        if (canceled) {
            mainHandler.post(() -> callback.onFailure(this, new IOException("Canceled")));
            return;
        }

        okhttp3.Call call = newCall();
        synchronized (this) {
            pendingCall = call;
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(OkHttpUploadCall.this, e));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) {
                try (okhttp3.Response closeableResponse = rawResponse) {
                    Response<T> response = toResponse(closeableResponse);
                    mainHandler.post(() -> callback.onResponse(OkHttpUploadCall.this, response));
                } catch (IOException e) {
                    mainHandler.post(() -> callback.onFailure(OkHttpUploadCall.this, e));
                }
            }
        });
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public synchronized void cancel() {
        canceled = true;
        if (pendingCall != null) {
            pendingCall.cancel();
        }
    }

    @Override
    public synchronized boolean isCanceled() {
        return canceled;
    }

    @Override
    public Call<T> clone() {
        return new OkHttpUploadCall<>(client, url, filePart, responseType);
    }

    private synchronized void markExecuted() {
        if (executed) {
            throw new IllegalStateException("Already executed");
        }
        executed = true;
    }

    private okhttp3.Call newCall() {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(filePart)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return client.newCall(request);
    }

    private Response<T> toResponse(okhttp3.Response rawResponse) throws IOException {
        ResponseBody body = rawResponse.body();
        byte[] bytes = body != null ? body.bytes() : new byte[0];
        if (!rawResponse.isSuccessful()) {
            return Response.error(rawResponse.code(), ResponseBody.create(bytes, MediaType.parse("application/json")));
        }

        if (responseType == Void.class || responseType == void.class) {
            return Response.success(null);
        }

        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        T parsed = gson.fromJson(json, responseType);
        return Response.success(parsed);
    }
}
