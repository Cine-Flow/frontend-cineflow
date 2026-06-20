package com.android.cineflow.data.network;

import java.io.IOException;

public interface Call<T> extends Cloneable {
    Response<T> execute() throws IOException;
    void enqueue(Callback<T> callback);
    boolean isExecuted();
    void cancel();
    boolean isCanceled();
    Call<T> clone();
}
