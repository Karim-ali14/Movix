package com.karimali.movix.features.movies.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.karimali.movix.core.network.toDomainException
import com.karimali.movix.core.util.MoviesConstants.FIRST_PAGE
import com.karimali.movix.features.movies.data.datasource.local.MoviesLocalDataSource
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.local.entity.RemoteKeysEntity
import com.karimali.movix.features.movies.data.datasource.remote.MoviesRemoteDataSource
import com.karimali.movix.features.movies.data.mapper.toEntityOrNull
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalPagingApi::class)
class MoviesRemoteMediator(
    private val remote: MoviesRemoteDataSource,
    private val local: MoviesLocalDataSource,
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun initialize() = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> FIRST_PAGE

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = false)

                val keys = local.remoteKeys(lastItem.id)
                    ?: return MediatorResult.Error(
                        IllegalStateException("Missing remote keys for movie ${lastItem.id}")
                            .toDomainException()
                    )

                keys.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val response = remote.fetchMovies(page)
            val rawResults = response.results.orEmpty()

            val entities = rawResults.mapIndexedNotNull { index, dto ->
                dto.toEntityOrNull(page = page, position = index)
            }

            val endReached = rawResults.isEmpty() || page >= (response.totalPages ?: page)

            val keys = entities.map {
                RemoteKeysEntity(
                    movieId = it.id,
                    prevKey = if (page == FIRST_PAGE) null else page - 1,
                    nextKey = if (endReached) null else page + 1,
                )
            }

            local.savePage(entities, keys, clearFirst = loadType == LoadType.REFRESH)
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MediatorResult.Error(e.toDomainException())
        }
    }
}