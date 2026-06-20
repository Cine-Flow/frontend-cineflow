package com.android.cineflow.data.network;

import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttp3Stack extends BaseHttpStack {
    private final OkHttpClient client;

    public OkHttp3Stack(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, com.android.volley.AuthFailureError {
        
        int timeoutMs = request.getTimeoutMs();
        OkHttpClient.Builder clientBuilder = client.newBuilder();
        clientBuilder.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.readTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.writeTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        OkHttpClient okClient = clientBuilder.build();

        okhttp3.Request.Builder okRequestBuilder = new okhttp3.Request.Builder();
        okRequestBuilder.url(request.getUrl());

        // Copy standard Volley headers
        Map<String, String> headers = request.getHeaders();
        if (headers != null) {
            for (String name : headers.keySet()) {
                okRequestBuilder.addHeader(name, headers.get(name));
            }
        }
        
        // Copy additional headers
        if (additionalHeaders != null) {
            for (String name : additionalHeaders.keySet()) {
                okRequestBuilder.addHeader(name, additionalHeaders.get(name));
            }
        }

        setConnectionParametersForRequest(okRequestBuilder, request);

        okhttp3.Request okRequest = okRequestBuilder.build();
        Call okHttpCall = okClient.newCall(okRequest);
        Response okHttpResponse = okHttpCall.execute();

        int responseCode = okHttpResponse.code();
        ResponseBody body = okHttpResponse.body();
        
        // Convert headers
        Headers okHeaders = okHttpResponse.headers();
        List<Header> volleyHeaders = new ArrayList<>();
        for (int i = 0, len = okHeaders.size(); i < len; i++) {
            volleyHeaders.add(new Header(okHeaders.name(i), okHeaders.value(i)));
        }

        int contentLength = 0;
        java.io.InputStream contentStream = null;
        if (body != null) {
            contentLength = (int) body.contentLength();
            contentStream = body.byteStream();
        }

        return new HttpResponse(responseCode, volleyHeaders, contentLength, contentStream);
    }

    private static void setConnectionParametersForRequest(okhttp3.Request.Builder builder, Request<?> request)
            throws IOException, com.android.volley.AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(RequestBody.create(postBody, MediaType.parse(request.getPostBodyContentType())));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static RequestBody createRequestBody(Request<?> request) throws com.android.volley.AuthFailureError {
        byte[] body = request.getBody();
        if (body == null) {
            return RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
        }
        String contentType = request.getBodyContentType();
        return RequestBody.create(body, contentType != null ? MediaType.parse(contentType) : MediaType.parse("application/json; charset=utf-8"));
    }
}
