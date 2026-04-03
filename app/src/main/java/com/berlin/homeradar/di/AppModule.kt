package com.berlin.homeradar.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.berlin.homeradar.data.local.AppDatabase
import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.remote.api.RemoteListingsService
import com.berlin.homeradar.data.repository.HousingRepositoryImpl
import com.berlin.homeradar.domain.repository.HousingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "berlin_home_radar.db",
        ).build()
    }

    @Provides
    fun provideHousingListingDao(database: AppDatabase): HousingListingDao = database.housingListingDao()

    @Provides
    fun provideSyncStatusDao(database: AppDatabase): SyncStatusDao = database.syncStatusDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideRemoteListingsService(retrofit: Retrofit): RemoteListingsService {
        return retrofit.create(RemoteListingsService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {
    @Binds
    abstract fun bindHousingRepository(impl: HousingRepositoryImpl): HousingRepository
}
