package com.gianlucaparadise.memorandaloco.di

import android.content.Context
import androidx.room.Room
import com.gianlucaparadise.memorandaloco.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "memoranda-loco-db"
        ).build()
    }
}