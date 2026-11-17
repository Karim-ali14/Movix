package com.karimali.movix.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.karimali.movix.features.details.data.datasource.local.DetailsTypeConverters
import com.karimali.movix.features.details.data.datasource.local.dao.MovieDetailsDao
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import com.karimali.movix.features.movies.data.datasource.local.MoviesTypeConverters
import com.karimali.movix.features.movies.data.datasource.local.dao.MoviesDao
import com.karimali.movix.features.movies.data.datasource.local.dao.RemoteKeysDao
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.local.entity.RemoteKeysEntity

@Database(
    entities = [MovieEntity::class, RemoteKeysEntity::class, MovieDetailsEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    MoviesTypeConverters::class, DetailsTypeConverters::class
)
abstract class MovixDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
    abstract fun movieDetailsDao(): MovieDetailsDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        const val NAME = "movix.db"
    }
}