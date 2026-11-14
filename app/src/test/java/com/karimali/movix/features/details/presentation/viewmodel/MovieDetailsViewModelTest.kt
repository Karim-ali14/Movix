package com.karimali.movix.features.details.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.testutil.MainDispatcherRule
import com.karimali.movix.core.ui.asDomainError
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.domain.usecase.ObserveMovieDetailsUseCase
import com.karimali.movix.features.details.domain.usecase.RefreshMovieDetailsUseCase
import com.karimali.movix.features.details.presentation.contract.MovieDetailsEffect
import com.karimali.movix.features.details.presentation.contract.MovieDetailsIntent
import com.karimali.movix.features.details.presentation.contract.MovieDetailsUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.net.SocketTimeoutException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MovieDetailsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val observeUseCase = mockk<ObserveMovieDetailsUseCase>()
    private val refreshUseCase = mockk<RefreshMovieDetailsUseCase>()

    private val cache = MutableStateFlow<MovieDetails?>(null)

    companion object {
        private const val MOVIE_ID = 42
        private const val HOMEPAGE = "https://example.com"
        private const val IMDB_URL = "https://imdb.com/title/tt123"
    }

    @Before
    fun setUp() {
        every { observeUseCase(any()) } returns cache
        coEvery { refreshUseCase(any()) } returns Result.success(Unit)
    }

    private fun createViewModel(movieId: Int = MOVIE_ID) = MovieDetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("movieId" to movieId)),
        observeMovieDetails = observeUseCase,
        refreshMovieDetails = refreshUseCase,
    )

    private fun refreshBlockedBy(gate: CompletableDeferred<Unit> = CompletableDeferred()): CompletableDeferred<Unit> {
        coEvery { refreshUseCase(any()) } coAnswers {
            gate.await()
            Result.success(Unit)
        }
        return gate
    }

    private fun assertMappedFrom(cause: Throwable, actual: DomainError?) {
        assertNotNull("Expected an error in state", actual)
        assertEquals(cause.asDomainError()::class, actual!!::class)
    }

    // ───────────────────────── Initialization ─────────────────────────

    @Test
    fun `init observes and refreshes using the movie id from the route`() = runTest {
        val routeMovieId = 42
        createViewModel(movieId = routeMovieId)

        verify(exactly = 1) { observeUseCase(routeMovieId) }
        coVerify(exactly = 1) { refreshUseCase(routeMovieId) }
    }

    @Test
    fun `init triggers exactly one refresh`() = runTest {
        createViewModel()
        coVerify(exactly = 1) { refreshUseCase(any()) }
    }

    @Test
    fun `initial state is refreshing with no movie and no error`() = runTest {
        refreshBlockedBy()
        val vm = createViewModel()

        assertEquals(MovieDetailsUiState(isRefreshing = true), vm.state.value)
    }

    // ───────────────────────── Cache ─────────────────────────

    @Test
    fun `cached movie is shown while the refresh is still running`() = runTest {
        val cached = createMovieDetails(title = "Cached")
        cache.value = cached
        refreshBlockedBy()

        val vm = createViewModel()

        assertEquals(MovieDetailsUiState(movie = cached, isRefreshing = true), vm.state.value)
    }

    @Test
    fun `null cache emission is ignored`() = runTest {
        cache.value = null
        refreshBlockedBy()

        val vm = createViewModel()

        assertNull(vm.state.value.movie)
    }

    @Test
    fun `later cache updates are reflected in state`() = runTest {
        cache.value = createMovieDetails(title = "Old")
        val vm = createViewModel()
        val updated = createMovieDetails(title = "New")

        cache.value = updated

        assertEquals(updated, vm.state.value.movie)
    }

    @Test
    fun `successful refresh pushes fresh data through the cache`() = runTest {
        val fresh = createMovieDetails(title = "Fresh")
        coEvery { refreshUseCase(any()) } coAnswers {
            cache.value = fresh
            Result.success(Unit)
        }

        val vm = createViewModel()

        assertEquals(MovieDetailsUiState(movie = fresh, isRefreshing = false), vm.state.value)
    }

    @Test
    fun `successful refresh with unchanged cache keeps the movie and stops refreshing`() = runTest {
        val cached = createMovieDetails()
        cache.value = cached

        val vm = createViewModel()

        assertEquals(MovieDetailsUiState(movie = cached, isRefreshing = false), vm.state.value)
    }

    @Test
    fun `state goes loading then cached then finished`() = runTest {
        val cached = createMovieDetails()
        val gate = refreshBlockedBy()
        val vm = createViewModel()

        vm.state.test {
            assertEquals(MovieDetailsUiState(isRefreshing = true), awaitItem())

            cache.value = cached
            assertEquals(MovieDetailsUiState(movie = cached, isRefreshing = true), awaitItem())

            gate.complete(Unit)
            assertEquals(MovieDetailsUiState(movie = cached, isRefreshing = false), awaitItem())
        }
    }

    // ───────────────────────── Refresh failure ─────────────────────────

    @Test
    fun `failure without cache exposes a full-screen error and no movie`() = runTest {
        val cause = IOException("offline")
        coEvery { refreshUseCase(any()) } returns Result.failure(cause)

        val vm = createViewModel()

        val state = vm.state.value
        assertNull(state.movie)
        assertEquals(false, state.isRefreshing)
        assertMappedFrom(cause, state.error)
    }

    @Test
    fun `failure with cache keeps the cached movie and exposes the error`() = runTest {
        val cached = createMovieDetails()
        val cause = SocketTimeoutException("timeout")
        cache.value = cached
        coEvery { refreshUseCase(any()) } returns Result.failure(cause)

        val vm = createViewModel()

        val state = vm.state.value
        assertEquals(cached, state.movie)
        assertEquals(false, state.isRefreshing)
        assertMappedFrom(cause, state.error)
    }

    @Test
    fun `fast failure reads the db directly when the collector has not emitted yet`() = runTest {
        val cached = createMovieDetails()
        every { observeUseCase(any()) } returnsMany listOf<Flow<MovieDetails?>>(
            flow { awaitCancellation() },
            flowOf(cached),
        )
        coEvery { refreshUseCase(any()) } returns Result.failure(IOException("offline"))

        val vm = createViewModel()

        assertEquals(cached, vm.state.value.movie)
        assertNotNull(vm.state.value.error)
        verify(exactly = 2) { observeUseCase(any()) }
    }

    @Test
    fun `failure with nothing in the db leaves movie null`() = runTest {
        every { observeUseCase(any()) } returns flowOf(null)
        coEvery { refreshUseCase(any()) } returns Result.failure(IOException("offline"))

        val vm = createViewModel()

        assertNull(vm.state.value.movie)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `unexpected exceptions are also mapped to a domain error`() = runTest {
        val cause = IllegalStateException("bad payload")
        coEvery { refreshUseCase(any()) } returns Result.failure(cause)

        val vm = createViewModel()

        assertMappedFrom(cause, vm.state.value.error)
    }

    @Test
    fun `late cache emission after a failure keeps the error so the screen shows a banner`() =
        runTest {
            coEvery { refreshUseCase(any()) } returns Result.failure(IOException("offline"))
            val vm = createViewModel()
            val arrived = createMovieDetails()

            cache.value = arrived

            assertEquals(arrived, vm.state.value.movie)
            assertNotNull(vm.state.value.error)
        }

    // ───────────────────────── Retry ─────────────────────────

    @Test
    fun `retry after a failure refetches and clears the error on success`() = runTest {
        val fresh = createMovieDetails(title = "Recovered")
        coEvery { refreshUseCase(any()) } returnsMany listOf(
            Result.failure(IOException("offline")),
            Result.success(Unit),
        )
        val vm = createViewModel()
        assertNotNull(vm.state.value.error)

        cache.value = fresh
        vm.onIntent(MovieDetailsIntent.Retry)

        assertEquals(MovieDetailsUiState(movie = fresh, isRefreshing = false), vm.state.value)
        coVerify(exactly = 2) { refreshUseCase(any()) }
    }

    @Test
    fun `retry clears the error immediately and shows refreshing`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var attempt = 0
        coEvery { refreshUseCase(any()) } coAnswers {
            if (attempt++ == 0) {
                Result.failure(IOException("offline"))
            } else {
                gate.await()
                Result.success(Unit)
            }
        }
        val vm = createViewModel()

        vm.onIntent(MovieDetailsIntent.Retry)

        assertEquals(MovieDetailsUiState(isRefreshing = true, error = null), vm.state.value)
        gate.complete(Unit)
    }

    @Test
    fun `retry keeps cached content visible while loading`() = runTest {
        val cached = createMovieDetails()
        val gate = CompletableDeferred<Unit>()
        var attempt = 0
        cache.value = cached
        coEvery { refreshUseCase(any()) } coAnswers {
            if (attempt++ == 0) {
                Result.failure(IOException("offline"))
            } else {
                gate.await()
                Result.success(Unit)
            }
        }
        val vm = createViewModel()

        vm.onIntent(MovieDetailsIntent.Retry)

        assertEquals(MovieDetailsUiState(movie = cached, isRefreshing = true), vm.state.value)
        gate.complete(Unit)
    }

    @Test
    fun `retry that fails again exposes the error again`() = runTest {
        val cause = IOException("still offline")
        coEvery { refreshUseCase(any()) } returns Result.failure(cause)
        val vm = createViewModel()

        vm.onIntent(MovieDetailsIntent.Retry)

        coVerify(exactly = 2) { refreshUseCase(any()) }
        assertEquals(false, vm.state.value.isRefreshing)
        assertMappedFrom(cause, vm.state.value.error)
    }

    @Test
    fun `retry is ignored while a refresh is already running`() = runTest {
        refreshBlockedBy()
        val vm = createViewModel()

        vm.onIntent(MovieDetailsIntent.Retry)
        vm.onIntent(MovieDetailsIntent.Retry)

        coVerify(exactly = 1) { refreshUseCase(any()) }
    }

    // ───────────────────────── Effects ─────────────────────────

    @Test
    fun `back click emits NavigateBack`() = runTest {
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.BackClicked)
            assertEquals(MovieDetailsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `website click opens the homepage`() = runTest {
        cache.value = createMovieDetails()
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.WebsiteClicked)
            assertEquals(MovieDetailsEffect.OpenUrl(HOMEPAGE), awaitItem())
        }
    }

    @Test
    fun `imdb click opens the imdb url`() = runTest {
        cache.value = createMovieDetails()
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.ImdbClicked)
            assertEquals(MovieDetailsEffect.OpenUrl(IMDB_URL), awaitItem())
        }
    }

    @Test
    fun `website click emits nothing when the movie has no homepage`() = runTest {
        cache.value = createMovieDetails(homepage = null)
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.WebsiteClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `imdb click emits nothing when the movie has no imdb url`() = runTest {
        cache.value = createMovieDetails(imdbUrl = null)
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.ImdbClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `blank urls are ignored`() = runTest {
        cache.value = createMovieDetails(homepage = "   ", imdbUrl = "")
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.WebsiteClicked)
            vm.onIntent(MovieDetailsIntent.ImdbClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `link clicks before the movie is loaded emit nothing`() = runTest {
        refreshBlockedBy()
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.WebsiteClicked)
            vm.onIntent(MovieDetailsIntent.ImdbClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `effects are delivered in the order they were triggered`() = runTest {
        cache.value = createMovieDetails()
        val vm = createViewModel()

        vm.effects.test {
            vm.onIntent(MovieDetailsIntent.WebsiteClicked)
            vm.onIntent(MovieDetailsIntent.ImdbClicked)
            vm.onIntent(MovieDetailsIntent.BackClicked)

            assertEquals(MovieDetailsEffect.OpenUrl(HOMEPAGE), awaitItem())
            assertEquals(MovieDetailsEffect.OpenUrl(IMDB_URL), awaitItem())
            assertEquals(MovieDetailsEffect.NavigateBack, awaitItem())
        }
    }


    private fun createMovieDetails(
        id: Int = MOVIE_ID,
        title: String = "Star Wars",
        originalTitle: String = title,
        tagline: String = "A long time ago in a galaxy far, far away...",
        overview: String = "Princess Leia is captured and held hostage by the evil Imperial forces.",
        posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl: String? = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        releaseDate: String = "1977-05-25",
        runtimeMinutes: Int? = 121,
        rating: Double = 8.2,
        voteCount: Int = 22_061,
        status: String = "Released",
        originalLanguageCode: String = "en",
        budget: Long = 11_000_000L,
        revenue: Long = 775_398_007L,
        homepage: String? = HOMEPAGE,
        imdbUrl: String? = IMDB_URL,
        collectionName: String? = "Star Wars Collection",
        genres: List<String> = listOf("Adventure", "Action", "Science Fiction"),
        productionCompanies: List<String> = listOf("Lucasfilm Ltd.", "20th Century Fox"),
        spokenLanguages: List<String> = listOf("English"),
    ) = MovieDetails(
        id = id,
        title = title,
        originalTitle = originalTitle,
        tagline = tagline,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        runtimeMinutes = runtimeMinutes,
        rating = rating,
        voteCount = voteCount,
        status = status,
        originalLanguageCode = originalLanguageCode,
        budget = budget,
        revenue = revenue,
        homepage = homepage,
        imdbUrl = imdbUrl,
        collectionName = collectionName,
        genres = genres,
        productionCompanies = productionCompanies,
        spokenLanguages = spokenLanguages,
    )
}