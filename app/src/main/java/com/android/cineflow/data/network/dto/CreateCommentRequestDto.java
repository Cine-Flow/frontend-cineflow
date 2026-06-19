package com.android.cineflow.data.network.dto;

public class CreateCommentRequestDto {
    private final String content;

    public CreateCommentRequestDto(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
}
