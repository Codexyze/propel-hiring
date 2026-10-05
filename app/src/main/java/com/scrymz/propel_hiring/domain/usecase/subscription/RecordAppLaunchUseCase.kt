package com.scrymz.propel_hiring.domain.usecase.subscription

import com.scrymz.propel_hiring.core.analytics.Analytics
import com.scrymz.propel_hiring.domain.repository.subscription.SubscriptionRepository
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class RecordAppLaunchUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
    private val analytics: Analytics
) {
    suspend operator fun invoke(): Flow<ResultState<Int>> {
        return repository.incrementLaunchCount().onEach { result ->
            if (result is ResultState.Success) {
                // Analytics Moment 1: app_launched
                analytics.logEvent("app_launched", mapOf("launch_count" to result.data))
            }
        }
    }
}
