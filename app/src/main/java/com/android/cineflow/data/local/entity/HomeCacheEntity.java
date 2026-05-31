package com.android.cineflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "home_cache")
public class HomeCacheEntity {
    @PrimaryKey
    @NonNull
    public String id = "home_data";

    public String jsonData;

    public HomeCacheEntity(String jsonData) {
        this.jsonData = jsonData;
    }
}
