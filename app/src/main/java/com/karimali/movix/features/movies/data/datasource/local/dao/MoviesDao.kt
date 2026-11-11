package com.karimali.movix.features.movies.data.datasource.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity

@Dao
interface MoviesDao {
    @Query("SELECT * FROM movies ORDER BY page ASC, position ASC")
    fun pagingSource(): PagingSource<Int, MovieEntity>

    @Query(
        """
        SELECT * FROM movies
        WHERE title LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY page ASC, position ASC
        LIMIT 100
        """
    )
    suspend fun searchCached(query: String): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    suspend fun clearAll()
}