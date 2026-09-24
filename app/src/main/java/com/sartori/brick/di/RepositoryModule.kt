package com.sartori.brick.di

import com.sartori.brick.data.earthquake.DefaultEarthquakeRepository
import com.sartori.brick.data.earthquake.EarthquakeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindEarthquakeRepository(
        repository: DefaultEarthquakeRepository
    ): EarthquakeRepository
}
