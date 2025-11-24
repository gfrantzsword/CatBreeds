package com.example.catbreeds.data.di

import android.content.Context
import androidx.room.Room
import com.example.catbreeds.data.connectivity.ConnectivityCheckerImpl
import com.example.catbreeds.data.local.AppDatabase
import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.data.local.FavoriteDao
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.data.remote.RetrofitInstance
import com.example.catbreeds.data.repository.BreedRepositoryImpl
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.core.util.ConnectivityChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideRemoteService(): RemoteService {
        return RetrofitInstance.api
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "breed_database"
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideBreedDao(appDatabase: AppDatabase): BreedDao {
        return appDatabase.breedDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(appDatabase: AppDatabase): FavoriteDao {
        return appDatabase.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideConnectivityChecker(
        @ApplicationContext context: Context
    ): ConnectivityChecker {
        return ConnectivityCheckerImpl(context)
    }

    @Provides
    @Singleton
    fun provideBreedRepository(
        remoteService: RemoteService,
        localSource: BreedDao,
        favoriteDao: FavoriteDao,
        connectivityChecker: ConnectivityChecker
    ): BreedRepository {
        return BreedRepositoryImpl(remoteService, localSource, favoriteDao, connectivityChecker)
    }
}