package com.karimali.movix.features.movies.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.karimali.movix.core.network.toDomainException
import com.karimali.movix.core.util.MoviesConstants.FIRST_PAGE
import com.karimali.movix.features.movies.data.datasource.local.MoviesLocalDataSource
import com.karimali.movix.features.movies.data.datasource.remote.MoviesRemoteDataSource
import com.karimali.movix.features.movies.data.mapper.toDomain
import com.karimali.movix.features.movies.data.mapper.toDomainOrNull
import com.karimali.movix.features.movies.domain.model.Movie
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SearchMoviesPagingSource(
    private val remote: MoviesRemoteDataSource,
    private val local: MoviesLocalDataSource,
    private val query: String,
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = remote.searchMovies(query, page)
            val raw = response.results.orEmpty()
            val endReached = raw.isEmpty() || page >= (response.totalPages ?: page)

            LoadResult.Page(
                data = raw.mapNotNull { it.toDomainOrNull() },
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (endReached) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            offlineFallback(page, e)
        } catch (e: Exception) {
            LoadResult.Error(e.toDomainException())
        }
    }

    private suspend fun offlineFallback(
        page: Int,
        original: IOException,
    ): LoadResult<Int, Movie> {
        val cached = if (page == FIRST_PAGE) {
            try {
                local.searchCached(query).map { it.toDomain() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return if (cached.isNotEmpty()) {
            LoadResult.Page(data = cached, prevKey = null, nextKey = null)
        } else {
            LoadResult.Error(original.toDomainException())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
}