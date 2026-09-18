package com.scrymz.propel_hiring.di.modules.subscription

import com.scrymz.propel_hiring.data.local.datastore.SubscriptionDataStore
import com.scrymz.propel_hiring.data.repo.subscription.SubscriptionRepositoryImpl
import com.scrymz.propel_hiring.domain.repository.subscription.SubscriptionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubscriptionModule {

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        dataStore: SubscriptionDataStore
    ): SubscriptionRepository {
        return SubscriptionRepositoryImpl(dataStore)
    }
}
