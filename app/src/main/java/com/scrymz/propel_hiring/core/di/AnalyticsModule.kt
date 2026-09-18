package com.scrymz.propel_hiring.core.di

import com.scrymz.propel_hiring.analytics.Analytics
import com.scrymz.propel_hiring.analytics.LogcatAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalytics(): Analytics = LogcatAnalytics()
}
