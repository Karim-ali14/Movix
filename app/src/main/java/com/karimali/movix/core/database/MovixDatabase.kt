package com.karimali.movix.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.karimali.movix.features.movies.data.datasource.local.MoviesTypeConverters
import com.karimali.movix.features.movies.data.datasource.local.dao.MoviesDao
import com.karimali.movix.features.movies.data.datasource.local.dao.RemoteKeysDao
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.local.entity.RemoteKeysEntity

@Database(
    entities = [MovieEntity::class, RemoteKeysEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(MoviesTypeConverters::class)
abstract class MovixDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        const val NAME = "movix.db"
    }
}