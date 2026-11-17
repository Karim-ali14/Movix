package com.karimali.movix.core.di

import android.content.Context
import androidx.room.Room
import com.karimali.movix.core.database.MovixDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MovixDatabase =
        Room.databaseBuilder(context, MovixDatabase::class.java, MovixDatabase.Companion.NAME)
            .fallbackToDestructiveMigration()
            .build()
    @Provides
    fun provideMoviesDao(db: MovixDatabase) = db.moviesDao()
    @Provides
    fun provideRemoteKeysDao(db: MovixDatabase) = db.remoteKeysDao()
}