package com.karimali.movix.features.details.data.di

import com.karimali.movix.features.details.data.repository.MovieDetailsRepositoryImpl
import com.karimali.movix.features.details.domain.repository.MovieDetailsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DetailsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieDetailsRepository(
        impl: MovieDetailsRepositoryImpl,
    ): MovieDetailsRepository
}