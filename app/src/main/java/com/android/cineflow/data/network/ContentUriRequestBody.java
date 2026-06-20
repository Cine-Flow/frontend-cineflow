package com.android.cineflow.data.network;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ContentUriRequestBody extends RequestBody {
    private final ContentResolver resolver;
    private final Uri uri;
    private final MediaType mediaType;
    private final long contentLength;

    public ContentUriRequestBody(ContentResolver resolver, Uri uri, String mimeType, long contentLength) {
        this.resolver = resolver;
        this.uri = uri;
        this.mediaType = MediaType.parse(mimeType != null ? mimeType : "application/octet-stream");
        this.contentLength = contentLength;
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (InputStream inputStream = resolver.openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Cannot open selected file");
            }

            OutputStream outputStream = sink.outputStream();
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
    }
}
