package com.karimali.movix.features.details.data.datasource.local

import com.karimali.movix.features.details.data.datasource.local.dao.MovieDetailsDao
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieDetailsLocalDataSource @Inject constructor(
    private val dao: MovieDetailsDao,
) {
    fun observe(movieId: Int): Flow<MovieDetailsEntity?> = dao.observe(movieId)

    suspend fun save(entity: MovieDetailsEntity) = dao.upsert(entity)
}