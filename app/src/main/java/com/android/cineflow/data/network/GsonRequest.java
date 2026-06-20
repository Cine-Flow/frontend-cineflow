package com.android.cineflow.data.network;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {
    private final Gson gson = new Gson();
    private final Type type;
    private final Response.Listener<T> listener;
    private final String bodyJson;
    private final Map<String, String> headers;

    public GsonRequest(int method, String url, Type type, String bodyJson,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, type, bodyJson, null, listener, errorListener);
    }

    public GsonRequest(int method, String url, Type type, String bodyJson,
                       Map<String, String> headers,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.type = type;
        this.bodyJson = bodyJson;
        this.headers = headers;
        this.listener = listener;
        setShouldCache(false); // Disable caching to fetch fresh data every time
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            return bodyJson == null ? null : bodyJson.getBytes("utf-8");
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            
            // Check if return type is Void or void
            if (type == Void.class || type == void.class) {
                return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
            }
            
            T parsedObject = gson.fromJson(json, type);
            return Response.success(
                    parsedObject,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}
