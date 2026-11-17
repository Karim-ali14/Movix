package com.karimali.movix.features.details.data.di

import com.karimali.movix.core.database.MovixDatabase
import com.karimali.movix.features.details.data.datasource.local.dao.MovieDetailsDao
import com.karimali.movix.features.details.data.datasource.remote.MovieDetailsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DetailsDataModule {

    @Provides
    @Singleton
    fun provideMovieDetailsApi(retrofit: Retrofit): MovieDetailsApi =
        retrofit.create(MovieDetailsApi::class.java)

    @Provides
    fun provideMovieDetailsDao(db: MovixDatabase): MovieDetailsDao = db.movieDetailsDao()
}