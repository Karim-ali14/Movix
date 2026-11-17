package com.karimali.movix.features.movies.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.karimali.movix.features.movies.data.datasource.local.MoviesLocalDataSource
import com.karimali.movix.features.movies.data.datasource.remote.MoviesRemoteDataSource
import com.karimali.movix.features.movies.data.mapper.toDomain
import com.karimali.movix.features.movies.data.paging.MoviesRemoteMediator
import com.karimali.movix.features.movies.data.paging.SearchMoviesPagingSource
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val remote: MoviesRemoteDataSource,
    private val local: MoviesLocalDataSource,
) : MoviesRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun fetchMovies(): Flow<PagingData<Movie>> = Pager(
        config = pagingConfig,
        remoteMediator = MoviesRemoteMediator(remote, local),
        pagingSourceFactory = { local.pagingSource() },
    ).flow.map { data -> data.map { it.toDomain() } }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = { SearchMoviesPagingSource(remote, local, query) },
    ).flow

    private companion object {
        const val PAGE_SIZE = 20

        val pagingConfig = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            prefetchDistance =  2,
            enablePlaceholders = false,
        )
    }
}