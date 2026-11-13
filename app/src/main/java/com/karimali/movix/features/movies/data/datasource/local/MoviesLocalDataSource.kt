package com.karimali.movix.features.movies.data.datasource.local

import androidx.room.withTransaction
import com.karimali.movix.features.movies.data.datasource.local.dao.MoviesDao
import com.karimali.movix.features.movies.data.datasource.local.dao.RemoteKeysDao
import com.karimali.movix.core.database.MovixDatabase
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.local.entity.RemoteKeysEntity
import javax.inject.Inject

class MoviesLocalDataSource @Inject constructor(
    private val db: MovixDatabase,
    private val moviesDao: MoviesDao,
    private val keysDao: RemoteKeysDao,
) {
    fun pagingSource() = moviesDao.pagingSource()

    suspend fun searchCached(query: String): List<MovieEntity> =
        moviesDao.searchCached(query.escapeForLike())

    suspend fun remoteKeys(movieId: Int) = keysDao.remoteKeysByMovieId(movieId)

    suspend fun savePage(
        movies: List<MovieEntity>,
        keys: List<RemoteKeysEntity>,
        clearFirst: Boolean,
    ) {
        db.withTransaction {
            if (clearFirst) {
                keysDao.clearAll()
                moviesDao.clearAll()
            }
            moviesDao.insertAll(movies)
            keysDao.insertAll(keys)
        }
    }

    private fun String.escapeForLike(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}