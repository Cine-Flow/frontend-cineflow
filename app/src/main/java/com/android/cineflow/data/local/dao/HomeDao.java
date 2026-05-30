package com.android.cineflow.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.cineflow.data.local.entity.HomeCacheEntity;

@Dao
public interface HomeDao {

    @Query("SELECT * FROM home_cache WHERE id = 'home_data' LIMIT 1")
    HomeCacheEntity getHomeCache();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHomeCache(HomeCacheEntity cacheEntity);

    @Query("DELETE FROM home_cache")
    void clearHomeCache();
}
