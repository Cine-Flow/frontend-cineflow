package com.android.cineflow.data.network;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

public class VolleyCall<T> implements Call<T> {
    private final String url;
    private final int method;
    private final String bodyJson;
    private final Type responseType;
    private final RequestQueue requestQueue;
    
    private boolean executed = false;
    private boolean canceled = false;
    private GsonRequest<T> pendingRequest;

    public VolleyCall(String url, int method, String bodyJson, Type responseType, RequestQueue requestQueue) {
        this.url = url;
        this.method = method;
        this.bodyJson = bodyJson;
        this.responseType = responseType;
        this.requestQueue = requestQueue;
    }

    @Override
    public Response<T> execute() throws IOException {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already executed");
            }
            executed = true;
        }

        if (canceled) {
            throw new IOException("Canceled");
        }

        RequestFuture<T> future = RequestFuture.newFuture();
        GsonRequest<T> request = new GsonRequest<>(
                method,
                url,
                responseType,
                bodyJson,
                future,
                future
        );
        
        synchronized (this) {
            pendingRequest = request;
        }
        
        requestQueue.add(request);

        try {
            T result = future.get();
            return Response.success(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Queue execution interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof VolleyError) {
                VolleyError volleyError = (VolleyError) cause;
                if (volleyError.networkResponse != null) {
                    int statusCode = volleyError.networkResponse.statusCode;
                    byte[] data = volleyError.networkResponse.data != null ? volleyError.networkResponse.data : new byte[0];
                    ResponseBody responseBody = ResponseBody.create(
                            data,
                            MediaType.parse("application/json")
                    );
                    return Response.error(statusCode, responseBody);
                }
            }
            throw new IOException(cause != null ? cause.getMessage() : "Volley execution failed", e);
        }
    }

    @Override
    public void enqueue(@NonNull Callback<T> callback) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already executed");
            }
            executed = true;
        }

        if (canceled) {
            callback.onFailure(this, new IOException("Canceled"));
            return;
        }

        GsonRequest<T> request = new GsonRequest<>(
                method,
                url,
                responseType,
                bodyJson,
                new com.android.volley.Response.Listener<T>() {
                    @Override
                    public void onResponse(T response) {
                        if (canceled) {
                            callback.onFailure(VolleyCall.this, new IOException("Canceled"));
                        } else {
                            callback.onResponse(VolleyCall.this, Response.success(response));
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (canceled) {
                            callback.onFailure(VolleyCall.this, new IOException("Canceled"));
                            return;
                        }
                        
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            byte[] data = error.networkResponse.data != null ? error.networkResponse.data : new byte[0];
                            ResponseBody responseBody = ResponseBody.create(
                                    data,
                                    MediaType.parse("application/json")
                            );
                            callback.onResponse(VolleyCall.this, Response.error(statusCode, responseBody));
                        } else {
                            callback.onFailure(VolleyCall.this, new IOException(
                                    error.getMessage() != null ? error.getMessage() : "Unknown network error", error));
                        }
                    }
                }
        );

        synchronized (this) {
            pendingRequest = request;
        }

        requestQueue.add(request);
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        synchronized (this) {
            canceled = true;
            if (pendingRequest != null) {
                pendingRequest.cancel();
            }
        }
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public Call<T> clone() {
        return new VolleyCall<>(url, method, bodyJson, responseType, requestQueue);
    }
}
