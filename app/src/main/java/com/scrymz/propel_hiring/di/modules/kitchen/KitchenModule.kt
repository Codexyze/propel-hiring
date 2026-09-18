package com.scrymz.propel_hiring.di.modules.kitchen

import com.scrymz.propel_hiring.data.local.kitchen.KitchenLocalDataSource
import com.scrymz.propel_hiring.data.repo.kitchen.KitchenRepositoryImpl
import com.scrymz.propel_hiring.domain.repository.kitchen.KitchenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KitchenModule {

    @Provides
    @Singleton
    fun provideKitchenRepository(
        localDataSource: KitchenLocalDataSource
    ): KitchenRepository {
        return KitchenRepositoryImpl(localDataSource)
    }
}
